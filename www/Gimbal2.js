/*
 * Cordova Plugin - Gimba v2
 * Denny Tsai <happydenn@happydenn.net>
 */

var cordova = require('cordova'),
    exec = require('cordova/exec');

var Gimbal2 = function() {
    this.hasInitialized = false;
};

Gimbal2.prototype.initialize = function(apiKey) {
    if (this.hasInitialized) return;

    exec(this.eventCallback, this.errorCallback, "Gimbal2", "initialize", [apiKey]);
    this.hasInitialized = true;
};

Gimbal2.prototype.eventCallback = function(data) {
    if (data.event == 'onBeaconSighting') {
        cordova.fireWindowEvent('beaconsighting', data);
    }
    else if(data.event == 'onCommunicationPresentedPush'){
        cordova.fireWindowEvent('gimbal_communicationPresentedPush', data);
    }
    else if(data.event == 'onCommunicationPresentedVisit'){
        cordova.fireWindowEvent('gimbal_communicationPresentedVisit', data);
    }
};

Gimbal2.prototype.errorCallback = function(data) {

};

Gimbal2.prototype.startBeaconManager = function() {
    if (!this.hasInitialized) return;
    exec(null, null, "Gimbal2", "startBeaconManager", []);
};

Gimbal2.prototype.stopBeaconManager = function() {
    if (!this.hasInitialized) return;
    exec(null, null, "Gimbal2", "stopBeaconManager", []);
};

Gimbal2.prototype.registerForPush = function(gcm) {
    exec(this.eventCallback, this.errorCallback, "Gimbal2", "registerForPush", [gcm]);
};

var gimbal2 = new Gimbal2();
module.exports = gimbal2;
