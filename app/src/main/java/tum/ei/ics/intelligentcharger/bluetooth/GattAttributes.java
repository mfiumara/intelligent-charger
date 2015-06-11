package tum.ei.ics.intelligentcharger.bluetooth;

import java.util.HashMap;

    /**
     * Created by Mathias Gopp on 24.02.2015.
     *
     * Contains all the specific data from a bluetooth device
     */
    public class GattAttributes {

        //*********************************************************************************************************************
        //RCS
        //*********************************************************************************************************************
        public static String CH_RCS_RAW_SERVICE = "fc3b898f-fefa-4fb5-97dc-df8fab78e93e";
        public static String CH_RCS_RAW_DATA_WRITE = "6a652ce5-c50a-4157-b990-d4e7d9cc0a53";
        public static String CH_RCS_RAW_DATA_READ = "d195b840-0333-4e7a-8e0d-429499d3baa1";

        public static String CH_RCS_ACC_SERVICE = "0000ae00-973c-44ad-e411-5cb0f25d2907";
        public static String CH_RCS_ACC_RAW_DATA_READ = "0000ae01-973c-44ad-e411-5cb0f25d2907";
        public static String CH_RCS_ACC_RAW_DATA_WRITE = "0000ae02-973c-44ad-e411-5cb0f25d2907";

        //*********************************************************************************************************************
        //GENERAL
        //*********************************************************************************************************************

        public static String CHARACTERISTIC_UPDATE_NOTIFICATION = "00002902-0000-1000-8000-00805f9b34fb";

        //*********************************************************************************************************************
}
