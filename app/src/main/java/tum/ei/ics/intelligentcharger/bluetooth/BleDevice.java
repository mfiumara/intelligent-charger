package tum.ei.ics.intelligentcharger.bluetooth;

import java.io.Serializable;

/**
 * Created by Mathias Gopp on 24.02.2015.
 *
 * This class is used to transfer the properties of a Bluetooth device between services
 */
public class BleDevice implements Serializable{

    public enum ConnectionState {
        DISCONNECTED, CONNECTING, CONNECTED
    }

    private String m_sDeviceName = null;
    private String m_sDeviceAddress = null;
    private ConnectionState m_oConnectionState = ConnectionState.DISCONNECTED;

    public void setDeviceName(String deviceName)
    {
        m_sDeviceName = deviceName;
    }

    public String getDeviceName()
    {
        return m_sDeviceName;
    }

    public void setDeviceAddress(String deviceAddress)
    {
        m_sDeviceAddress = deviceAddress;
    }

    public String getDeviceAddress()
    {
        return m_sDeviceAddress;
    }

    public void setConnectionState(ConnectionState connectionState) { m_oConnectionState = connectionState; }

    public ConnectionState getConnectionState()
    {
        return m_oConnectionState;
    }

}
