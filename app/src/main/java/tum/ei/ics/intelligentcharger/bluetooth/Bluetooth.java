package tum.ei.ics.intelligentcharger.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import tum.ei.ics.intelligentcharger.Global;
import tum.ei.ics.intelligentcharger.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Bluetooth#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Bluetooth extends android.support.v4.app.Fragment implements View.OnClickListener{

    private static final String TAG = Bluetooth.class.getSimpleName();

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String ARG_SECTION_NUMBER = "section_number";

    private OnFragmentInteractionListener mListener;

    private static final String ACTION_STRING_HE2MT_SERVICE = "ToHemtService";

    private static final String BLE_DEVICE = "BleDevice";

    private BleDeviceListAdapter m_oBleDeviceListAdapter;
    private BluetoothAdapter m_oBluetoothAdapter;
    private boolean m_bScanning;
    private static final long SCAN_PERIOD = 10000;
    private Handler m_oHandler;
    private final static int REQUEST_ENABLE_BT = 1;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Bluetooth.
     */
    public static Bluetooth newInstance(String param1, String param2) {
        Bluetooth fragment = new Bluetooth();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putInt(ARG_SECTION_NUMBER, 2);
        fragment.setArguments(args);
        return fragment;
    }

    public Bluetooth() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String mParam1 = getArguments().getString(ARG_PARAM1);
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        Log.d(TAG, "onCreateMethod");
        //return inflater.inflate(R.layout.fragment_bluetooth, container, false);

        m_oHandler = new Handler();

        final BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        m_oBluetoothAdapter = bluetoothManager.getAdapter();

        if (m_oBluetoothAdapter == null) {
            Toast.makeText(getActivity(), "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            getActivity().finish();
            return container;
        }

        m_bScanning = false;

        View rootView = inflater.inflate(R.layout.fragment_bluetooth, container, false);

        Button scan = (Button) rootView.findViewById(R.id.buttonScan);
        scan.setOnClickListener(this);

        m_oBleDeviceListAdapter = new BleDeviceListAdapter();
        ListView listView = (ListView) rootView.findViewById(R.id.listViewBleDevices);
        listView.setAdapter(m_oBleDeviceListAdapter);
        listView.setOnItemClickListener(m_oDeviceClickedHandler);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setSelector(android.R.color.holo_blue_bright);

        return rootView;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
     mListener = null;
     }

     @Override
     public void onClick(View v) {
         switch (v.getId())
         {
             case R.id.buttonScan: Log.d(TAG, "Click Event");
                 startScan();
                break;
         }

     }

     /**
      * This interface must be implemented by activities that contain this
      * fragment to allow an interaction in this fragment to be communicated
      * to the activity and potentially other fragments contained in that
      * activity.
      * <p/>
      * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

    /**
     * @brief Starts scanning for bluetooth devices
     */
    public void startScan()
    {
        if(!m_bScanning)
        {
            scanForBleDevices(true);
        }
        else
        {
            scanForBleDevices(false);
        }
    }

    /**
     * @brief This function handels the scanning for bluetooth devices
     */
    private void scanForBleDevices(final boolean enable)
    {
        if (!m_oBluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else
        {
            if(enable)
            {
                m_oHandler.postDelayed(new Runnable()
                {
                    @Override
                    public void run() {
                        m_bScanning = false;
                        m_oBluetoothAdapter.stopLeScan(m_oBleScanCallback);
                    }
                }, SCAN_PERIOD);
                m_oBleDeviceListAdapter.clear();
                m_oBleDeviceListAdapter.notifyDataSetChanged();
                m_bScanning = true;
                m_oBluetoothAdapter.startLeScan(m_oBleScanCallback);
            }
            else
            {
                m_bScanning = false;
                m_oBluetoothAdapter.stopLeScan(m_oBleScanCallback);
            }

            getActivity().invalidateOptionsMenu();
        }
    }

    private BluetoothAdapter.LeScanCallback m_oBleScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device,final int rssi, byte[] scanRecord) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    m_oBleDeviceListAdapter.addDevice(device, rssi);
                    m_oBleDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    private AdapterView.OnItemClickListener m_oDeviceClickedHandler = new AdapterView.OnItemClickListener()
    {
        public void onItemClick(AdapterView parent, View v, int position, long id)
        {
            final BluetoothDevice device = m_oBleDeviceListAdapter.getDevice(position);
            if(device == null)
            {
                return;
            }

            String m_sDeviceName = device.getName();
            String m_sDeviceAddress = device.getAddress();

            BleDevice bleData = new BleDevice();
            bleData.setDeviceName(m_sDeviceName);
            bleData.setDeviceAddress(m_sDeviceAddress);

            m_oBluetoothAdapter.stopLeScan(m_oBleScanCallback);

            if (m_bScanning)
            {
                m_oBluetoothAdapter.stopLeScan(m_oBleScanCallback);
                m_bScanning = false;
            }

            //Save device address to settings
            SharedPreferences settings = getActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(Global.AUTOCONNECT_BLE_DEVICEADDRESS, m_sDeviceAddress);
            editor.putString(Global.AUTOCONNECT_BLE_DEVICENAME, m_sDeviceName);
            editor.apply();

            //broadcast device properties to service
            broadcastToHE2mtService(BLE_DEVICE,bleData);
        }
    };

    private void broadcastToHE2mtService(final String action, final BleDevice bleDevice)
    {
        final Intent intent = new Intent(action);
        intent.setAction(ACTION_STRING_HE2MT_SERVICE);
        intent.putExtra(BLE_DEVICE, bleDevice);
        getActivity().sendBroadcast(intent);
    }

    private class BleDeviceListAdapter extends BaseAdapter
    {
        private ArrayList<BluetoothDevice> m_oBleDevices;
        private ArrayList<Integer> m_oRssi;
        private LayoutInflater m_oInflator;

        public BleDeviceListAdapter()
        {
            super();
            m_oBleDevices = new ArrayList<BluetoothDevice>();
            m_oRssi = new ArrayList<Integer>();
            m_oInflator = getActivity().getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device, int rssi)
        {
            if(!m_oBleDevices.contains(device))
            {
                m_oBleDevices.add(device);
                m_oRssi.add(Integer.valueOf(rssi));
            }
        }

        public BluetoothDevice getDevice(int position)
        {
            return m_oBleDevices.get(position);
        }

        public void clear()
        {
            m_oBleDevices.clear();
            m_oRssi.clear();
        }

        @Override
        public int getCount()
        {
            return m_oBleDevices.size();
        }

        @Override
        public Object getItem(int i)
        {
            return m_oBleDevices.get(i);
        }

        @Override
        public long getItemId(int i)
        {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup)
        {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null)
            {
                view = m_oInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.textViewListItemDevice);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.textViewListItemName);
                viewHolder.deviceRssi = (TextView) view.findViewById(R.id.textViewListItemRssi);
                view.setTag(viewHolder);
            }
            else
            {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = m_oBleDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
            {
                viewHolder.deviceName.setText(deviceName);
            }
            else
            {
                viewHolder.deviceName.setText("unknown");
            }
            viewHolder.deviceAddress.setText(device.getAddress());
            viewHolder.deviceRssi.setText(String.valueOf(m_oRssi.get(i))+"dBm");

            return view;
        }
    }

    static class ViewHolder
    {
        TextView deviceName;
        TextView deviceAddress;
        TextView deviceRssi;
    }

}
