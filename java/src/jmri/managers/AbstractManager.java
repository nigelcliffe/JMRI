package jmri.managers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import javax.annotation.Nonnull;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract partial implementation for all Manager-type classes.
 * <P>
 * Note that this does not enforce any particular system naming convention at
 * the present time. They're just names...
 *
 * @author Bob Jacobsen Copyright (C) 2003
 */
abstract public class AbstractManager implements Manager, PropertyChangeListener, VetoableChangeListener {

    public AbstractManager() {
        registerSelf();
    }

    /*public AbstractManager(int order) {
     // register the result for later configuration
     xmlorder = order;
     registerSelf();
     }*/
    /**
     * By default, register this manager to store as configuration information.
     * Override to change that.
     *
     */
    protected void registerSelf() {
        log.debug("registerSelf for config of type {}", getClass());
        ConfigureManager cm = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cm != null) {
            cm.registerConfig(this, getXMLOrder());
            log.debug("registering for config of type {}", getClass());
        }
    }

    @Override
    abstract public int getXMLOrder();

    @Override
    public String makeSystemName(String s) {
        return getSystemPrefix() + typeLetter() + s;
    }

    @Override
    public void dispose() {
        ConfigureManager cm = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cm != null) {
            cm.deregister(this);
        }
        _tsys.clear();
        _tuser.clear();
    }

    protected Hashtable<String, NamedBean> _tsys = new Hashtable<>();   // stores known Turnout instances by system name
    protected Hashtable<String, NamedBean> _tuser = new Hashtable<>();   // stores known Turnout instances by user name

    /**
     * Locate an instance based on a system name. Returns null if no instance
     * already exists. This is intended to be used by concrete classes to
     * implement their getBySystemName method. We can't call it that here
     * because Java doesn't have polymorphic return types.
     *
     * @param systemName the system name
     * @return requested Turnout object or null if none exists
     */
    protected Object getInstanceBySystemName(String systemName) {
        return _tsys.get(systemName);
    }

    /**
     * Locate an instance based on a user name. Returns null if no instance
     * already exists. This is intended to be used by concrete classes to
     * implement their getBySystemName method. We cant call it that here because
     * Java doesn't have polymorphic return types.
     *
     * @param userName the user name
     * @return requested Turnout object or null if none exists
     */
    protected Object getInstanceByUserName(String userName) {
        return _tuser.get(userName);
    }

    /**
     * Locate an instance based on a system name. Returns null if no instance
     * already exists.
     *
     * @param systemName System Name of the required NamedBean
     * @return requested NamedBean object or null if none exists
     */
    @Override
    public NamedBean getBeanBySystemName(String systemName) {
        return _tsys.get(systemName);
    }

    /**
     * Locate an instance based on a user name. Returns null if no instance
     * already exists.
     *
     * @param userName System Name of the required NamedBean
     * @return requested NamedBean object or null if none exists
     */
    @Override
    public NamedBean getBeanByUserName(String userName) {
        return _tuser.get(userName);
    }

    /**
     * Locate an instance based on a name. Returns null if no instance already
     * exists.
     *
     * @param name System Name of the required NamedBean
     * @return requested NamedBean object or null if none exists
     */
    @Override
    public NamedBean getNamedBean(String name) {
        NamedBean b = getBeanByUserName(name);
        if (b != null) {
            return b;
        }
        return getBeanBySystemName(name);
    }

    /**
     * Method for a UI to delete a bean, the UI should first request a
     * "CanDelete", then if that comes back clear, or the user agrees with the
     * actions, then a "DoDelete" can be called which inform the listeners to
     * delete the bean, then it will be deregistered and disposed of.
     *
     * @param bean     The NamedBean to be deleted
     * @param property The programmatic name of the property: "CanDelete" will
     *                 enquire with all listeners if the item can be deleted.
     *                 "DoDelete" tells the listener to delete the item
     * @throws PropertyVetoException - If the recipient(s) wishes the delete to
     *                               be aborted.
     */
    @Override
    public void deleteBean(@Nonnull NamedBean bean, @Nonnull String property) throws PropertyVetoException {
        try {
            fireVetoableChange(property, bean, null);
        } catch (PropertyVetoException e) {
            throw e;  // don't go on to check for delete.
        }
        if (property.equals("DoDelete")) { //IN18N
            deregister(bean);
            bean.dispose();
        }
    }

    /**
     * Remember a NamedBean Object created outside the manager.
     * <P>
     * The non-system-specific SignalHeadManagers use this method extensively.
     *
     * @param s the bean to register
     */
    @Override
    public void register(NamedBean s) {
        String systemName = s.getSystemName();
        _tsys.put(systemName, s);
        String userName = s.getUserName();
        if (userName != null) {
            _tuser.put(userName, s);
        }
        firePropertyChange("length", null, _tsys.size());
        // listen for name and state changes to forward
        s.addPropertyChangeListener(this, "", "Manager");
    }

    /**
     * Forget a NamedBean Object created outside the manager.
     * <P>
     * The non-system-specific RouteManager uses this method.
     *
     * @param s the bean to forget
     */
    @Override
    public void deregister(NamedBean s) {
        s.removePropertyChangeListener(this);
        String systemName = s.getSystemName();
        _tsys.remove(systemName);
        String userName = s.getUserName();
        if (userName != null) {
            _tuser.remove(userName);
        }
        firePropertyChange("length", null, _tsys.size());
        // listen for name and state changes to forward
    }

    /**
     * The PropertyChangeListener interface in this class is intended to keep
     * track of user name changes to individual NamedBeans. It is not completely
     * implemented yet. In particular, listeners are not added to newly
     * registered objects.
     *
     * @param e the event
     */
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals("UserName")) {
            String old = (String) e.getOldValue();  // OldValue is actually system name
            String now = (String) e.getNewValue();
            NamedBean t = (NamedBean) e.getSource();
            if (old != null) {
                _tuser.remove(old);
            }
            if (now != null) {
                _tuser.put(now, t);
            }

            //called DisplayListName, as DisplayName might get used at some point by a NamedBean
            firePropertyChange("DisplayListName", old, now); //IN18N
        }
    }

    @Override
    public String[] getSystemNameArray() {
        String[] arr = new String[_tsys.size()];
        Enumeration<String> en = _tsys.keys();
        int i = 0;
        while (en.hasMoreElements()) {
            arr[i] = en.nextElement();
            i++;
        }
        java.util.Arrays.sort(arr);
        return arr;
    }

    @Override
    public List<String> getSystemNameList() {
        String[] arr = new String[_tsys.size()];
        List<String> out = new ArrayList<>();
        Enumeration<String> en = _tsys.keys();
        int i = 0;
        while (en.hasMoreElements()) {
            arr[i] = en.nextElement();
            i++;
        }
        jmri.util.StringUtil.sort(arr);
        for (i = 0; i < arr.length; i++) {
            out.add(arr[i]);
        }
        return out;
    }

    @Override
    public List<NamedBean> getNamedBeanList() {
        return new ArrayList<>(_tsys.values());
    }

    @Override
    abstract public String getBeanTypeHandled();

    PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    VetoableChangeSupport vcs = new VetoableChangeSupport(this);

    @Override
    public synchronized void addVetoableChangeListener(VetoableChangeListener l) {
        vcs.addVetoableChangeListener(l);
    }

    @Override
    public synchronized void removeVetoableChangeListener(VetoableChangeListener l) {
        vcs.removeVetoableChangeListener(l);
    }

    /**
     * Method to inform all registered listerners of a vetoable change. If the
     * propertyName is "CanDelete" ALL listeners with an interest in the bean
     * will throw an exception, which is recorded returned back to the invoking
     * method, so that it can be presented back to the user. However if a
     * listener decides that the bean can not be deleted then it should throw an
     * exception with a property name of "DoNotDelete", this is thrown back up
     * to the user and the delete process should be aborted.
     *
     * @param p   The programmatic name of the property that is to be changed.
     *            "CanDelete" will enquire with all listerners if the item can
     *            be deleted. "DoDelete" tells the listerner to delete the item.
     * @param old The old value of the property.
     * @param n   The new value of the property.
     * @throws PropertyVetoException - if the recipients wishes the delete to be
     *                               aborted.
     */
    protected void fireVetoableChange(String p, Object old, Object n) throws PropertyVetoException {
        PropertyChangeEvent evt = new PropertyChangeEvent(this, p, old, n);
        if (p.equals("CanDelete")) { //IN18N
            StringBuilder message = new StringBuilder();
            for (VetoableChangeListener vc : vcs.getVetoableChangeListeners()) {
                try {
                    vc.vetoableChange(evt);
                } catch (PropertyVetoException e) {
                    if (e.getPropertyChangeEvent().getPropertyName().equals("DoNotDelete")) { //IN18N
                        log.info(e.getMessage());
                        throw e;
                    }
                    message.append(e.getMessage());
                    message.append("<hr>"); //IN18N
                }
            }
            throw new PropertyVetoException(message.toString(), evt);
        } else {
            try {
                vcs.fireVetoableChange(evt);
            } catch (PropertyVetoException e) {
                log.error("Change vetoed.", e);
            }
        }
    }

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {

        if ("CanDelete".equals(evt.getPropertyName())) { //IN18N
            StringBuilder message = new StringBuilder();
            message.append(Bundle.getMessage("VetoFoundIn", getBeanTypeHandled()))
                    .append("<ul>");
            boolean found = false;
            for (NamedBean nb : _tsys.values()) {
                try {
                    nb.vetoableChange(evt);
                } catch (PropertyVetoException e) {
                    if (e.getPropertyChangeEvent().getPropertyName().equals("DoNotDelete")) { //IN18N
                        throw e;
                    }
                    found = true;
                    message.append("<li>")
                            .append(e.getMessage())
                            .append("</li>");
                }
            }
            message.append("</ul>")
                    .append(Bundle.getMessage("VetoWillBeRemovedFrom", getBeanTypeHandled()));
            if (found) {
                throw new PropertyVetoException(message.toString(), evt);
            }
        } else {
            for (NamedBean nb : _tsys.values()) {
                try {
                    nb.vetoableChange(evt);
                } catch (PropertyVetoException e) {
                    throw e;
                }
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractManager.class.getName());

}
