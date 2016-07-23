package io.hpd.cordova.gimbal2;

/*
 * Cordova Plugin - Gimba v2
 * Denny Tsai <happydenn@happydenn.net>
 */

import java.util.Collection;
import java.util.Date;
import java.text.SimpleDateFormat;

import android.util.Log;

import com.gimbal.android.*;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Gimbal2 extends CordovaPlugin {

    private CallbackContext gimbalCallbackContext;
    private SimpleDateFormat simpleDateFormat;

    private BeaconManager beaconManager;
    private BeaconEventListener beaconEventListener;

    private CommunicationListener communicationListener;

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("initialize")) {
            if (args.length() != 1) return false;

            String apiKey = args.getString(0);
            Gimbal.setApiKey(cordova.getActivity().getApplication(), apiKey);

            if (gimbalCallbackContext == null) {
                gimbalCallbackContext = callbackContext;
            }

            if (simpleDateFormat == null) {
                simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            }

            beaconEventListener = new BeaconEventListener() {
                @Override
                public void onBeaconSighting(BeaconSighting beaconSighting) {
                    Log.i("INFO", "onBeaconSighting: " + beaconSighting.toString());

                    Beacon beacon = beaconSighting.getBeacon();

                    JSONObject responseObject = new JSONObject();

                    try {
                        responseObject.put("event", "onBeaconSighting");
                        responseObject.put("RSSI", beaconSighting.getRSSI());
                        responseObject.put("datetime", simpleDateFormat.format(new Date(beaconSighting.getTimeInMillis())));
                        responseObject.put("beaconName", beacon.getName());
                        responseObject.put("beaconIdentifier", beacon.getIdentifier());
                        responseObject.put("beaconBatteryLevel", beacon.getBatteryLevel());
                        responseObject.put("beaconIconUrl", beacon.getIconURL());
                        responseObject.put("beaconTemperature", beacon.getTemperature());
                    } catch (JSONException e) {
                        Log.e("Error", e.getMessage(), e);
                    }

                    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, responseObject);
                    pluginResult.setKeepCallback(true);

                    gimbalCallbackContext.sendPluginResult(pluginResult);
                }
            };

            beaconManager = new BeaconManager();
            beaconManager.addListener(beaconEventListener);

            communicationListener = new CommunicationListener(){
                @Override
                public Collection<Communication> presentNotificationForCommunications(Collection<Communication> communications, Push push) {
                    Log.i("INFO", "presentNotificationForCommunications (PUSH): " + push.toString());

                    for (Communication communication : communications) {
                        JSONObject responseObject = new JSONObject();

                        try {
                            responseObject.put("event", "onCommunicationPresentedPush");
                            responseObject.put("pushType", push.getPushType().toString());
                            responseObject.put("identifier", communication.getIdentifier());
                            responseObject.put("title", communication.getTitle());
                            responseObject.put("description", communication.getDescription());
                            responseObject.put("deliveryDate", simpleDateFormat.format(new Date(communication.getDeliveryDate())));
                            responseObject.put("expiryTimeInMillis", communication.getExpiryTimeInMillis());
                            responseObject.put("url", communication.getURL());
                            responseObject.put("attributes", parseAttributes(communication.getAttributes()));
                        } catch (JSONException e) {
                            Log.e("Error", e.getMessage(), e);
                        }

                        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, responseObject);
                        pluginResult.setKeepCallback(true);

                        gimbalCallbackContext.sendPluginResult(pluginResult);
                    }

                    return communications;
                }

                @Override
                public Collection<Communication> presentNotificationForCommunications(Collection<Communication> communications, Visit visit) {
                    Log.i("INFO", "presentNotificationForCommunications (VISIT): " + visit.toString());

                    for (Communication communication : communications) {
                        JSONObject responseObject = new JSONObject();

                        try {
                            responseObject.put("event", "onCommunicationPresentedVisit");
                            responseObject.put("identifier", communication.getIdentifier());
                            responseObject.put("title", communication.getTitle());
                            responseObject.put("description", communication.getDescription());
                            responseObject.put("deliveryDate", simpleDateFormat.format(new Date(communication.getDeliveryDate())));
                            responseObject.put("expiryTimeInMillis", communication.getExpiryTimeInMillis());
                            responseObject.put("url", communication.getURL());
                            responseObject.put("visit", parseVisit(visit));
                            responseObject.put("attributes", parseAttributes(communication.getAttributes()));
                        } catch (JSONException e) {
                            Log.e("Error", e.getMessage(), e);
                        }

                        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, responseObject);
                        pluginResult.setKeepCallback(true);

                        gimbalCallbackContext.sendPluginResult(pluginResult);
                    }

                    return communications;
                }

                private JSONObject parseVisit(Visit visit) throws JSONException {
                    JSONObject oVisit = new JSONObject();

                    oVisit.put("arrivalTime", simpleDateFormat.format(new Date(visit.getArrivalTimeInMillis())));
                    oVisit.put("departureTime", simpleDateFormat.format(new Date(visit.getDepartureTimeInMillis())));
                    oVisit.put("dwellTime", simpleDateFormat.format(new Date(visit.getDwellTimeInMillis())));
                    oVisit.put("visitId", visit.getVisitID());
                    oVisit.put("place", parsePlace(visit.getPlace()));

                    return oVisit;
                }

                private JSONObject parsePlace(Place place) throws JSONException {
                    JSONObject oPlace = null;
                    if(place != null){
                        oPlace = new JSONObject();
                        oPlace.put("identifier", place.getIdentifier());
                        oPlace.put("name", place.getName());
                        oPlace.put("attributes", parseAttributes(place.getAttributes()));
                    }

                    return oPlace;
                }

                private JSONObject parseAttributes(Attributes attrs) throws JSONException {
                    JSONObject oAttributes = null;
                    if(attrs != null) {
                        oAttributes = new JSONObject();
                        for (String k : attrs.getAllKeys()) {
                            oAttributes.put(k, attrs.getValue(k));
                        }
                    }

                    return oAttributes;
                }
            };

            CommunicationManager.getInstance().addListener(communicationListener);

            return true;
        }

        if (action.equals("startBeaconManager")) {
            if (beaconManager == null) return false;

            beaconManager.startListening();
            return true;
        }

        if (action.equals("stopBeaconManager")) {
            if (beaconManager == null) return false;

            beaconManager.stopListening();
            return true;
        }

        if (action.equals("registerForPush")) {
            if (args.length() != 1)
                return false;

            String gcm = args.getString(0);
            Gimbal.registerForPush(gcm);
            return true;
        }

        return false;
    }
}
