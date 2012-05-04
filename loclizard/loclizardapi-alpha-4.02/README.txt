                      ===========================
                         Android LocLizard API
                      ===========================

                                Alpha 4


1/ Introduction
2/ The API
3/ Integrating QPS into your existing project
   1. Getting started
   2. Upgrading
4/ Example application
5/ Advanced features
   1. Wi-Fi & GSM settings notifications
   2. DummyPositioningService
6/ Changelog


     1/ Introduction
     ---------------

The LocLizard API brings QPS (Qubulus Positioning System) to your own Android
applications via a Java/intent-based API wrapping a simple Android service
bundled with your apk.

Using the Wi-Fi and the GSM signals when available, QPS will return a position
estimate when the phone is in an area which has been scanned beforehand to
generate a "signal fingerprint" of the radio environment. The output of QPS
is a geo-coordinate expressed in the WGS 84 standard (widely used by GPS),
together with a map id, specific to how the site was scanned, most of the times
used to distinguish floors and provide z-level information.

This package contains:

 * This README.txt to get you started;
 * the JARs to be imported in your project, in QPSExample/libs/;
 * The API javadoc, in javadoc/;
 * The source code of an example application displaying the estimated position
   as returned by QPS, using the Google map rendering APIs, in QpsExample/.


     2/ The API
     ----------

Requirements:

1. The API is known to work on Android 2.1/Eclair (API level 7) and later.
   Previous versions of Android might be compatible, but they have not been
   tested and will not be supported.
2. Positioning will work only on the sites that have previously been scanned
   via the Gecko developer tool.
   See http://www.qubulus.com/developers/recording-tool/ for more information.
   Each scanned site is assigned a unique site id, that should have been
   communicated to you. (*)
3. You have received a customer id, which acts as an API key and will
   grant access to your application(s) on our servers. (*)
4. An active data connection is required, so that the client can forward
   the requests to the QPS server which will return the estimated position.
   Note that data traffic occurs only when requesting positions.

(*) NOTE: If you have not recorded your own site or been assigned a customer
          id yet, you can still integrate QPS via the DummyPositioningService
          described in chapter 5.2. When you later receive your site and/or
          customer id, switching to the real positioning is done with minimal
          effort.

An application developer can integrate the API to any project by adding the
jar files to the build path, declaring the Android service and configuring it
via Android resources. Due to the total size of the jars, you might want to use
obfuscation tools such as Proguard which is shipped with the latest Android SDK
releases, to shrink the code as much as possible.

An application developer should never access directly the positioning service,
but instead use the Java API in PositioningManager to send requests, and one or
several BroadcastReceiver to handle the intent ACTIONs and EXTRAs as defined in
Intents.


     3/ Integrating QPS into your existing project
     ---------------------------------------------

     3.1/ Getting started

1. Add *all* the JARs packaged in "QPSExample/libs/"to your build path

2. Declare the service in your AndroidManifest.xml:

     <service android:name="com.qubulus.qps.RemotePositioningService">
       <intent-filter>
         <action android:name="com.qubulus.qps.REQUEST" />
       </intent-filter>
     </service>

3. Add the required permissions to your AndroidManifest.xml.

     <uses-permission android:name="android.permission.WAKE_LOCK"/>
     <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
     <uses-permission android:name="android.permission.CHANGE_WIFI_STATE"/>
     <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
     <uses-permission android:name="android.permission.INTERNET"/>

4. Request positions where/when needed. In case you want regular updates, you
   will typically use Java's TimerTask in an Activity or Android's AlarmManager
   in a Service (probably with one of the *_WAKEUP flags) since there is no
   built-in support for recurrent request in the API.

     PositioningManager manager = PositioningManager.get(mContext);
     manager.requestPosition();

5. Implement and register a BroadcastReceiver handling the position estimates:

     private BroadcastReceiver mQpsReceiver = new BroadcastReceiver() {
       @Override
       public void onReceive(Context context, Intent intent) {
         // Implement your logic here (show position on the map for example)
       }
     };

     IntentFilter intentFilter = new IntentFilter();
     intentFilter.addAction(Intents.Position.ACTION);
     intentFilter.addAction(Intents.UnknwownPosition.ACTION);
     mContext.registerReceiver(mQpsReceiver, intentFilter);

   Read more about the information you can extract from the broadcasts in the
   javadoc for Intents.

6. Make sure you unregister your BroadcastReceiver and stop the service when
   appropriate (Activity#onPause() or Activity#onDestroy() for example) :

     mContext.unregisterReceiver(mQpsReceiver, intentFilter);

     PositioningManager manager = PositioningManager.get(mContext);
     manager.stopService();

7. Add to res/values/strings.xml the customer & site settings that were provided
   to you by e-mail. See the javadoc in RemotePositioningService for more
   information.

    <string name="qps_customer_id">YOUR_CUSTOMER_ID</string>
    <string name="qps_site_id">YOUR_SITE_ID</string>
    <string name="qps_server">api-dev.loclizard.com:8080</string>
    <string name="qps_switch_floor_queue_size">3</string>
    <string name="qps_switch_floor_queue_time_tollerance">30</string>


     3.2/ Upgrading

The API is backward-compatible until further notice, so if you have already
succesfully integrated QPS to your application using a previous LocLizard
release, you can simply copy/paste the new libraries and redeploy your
application. Just make sure you remove previous versions of the jar(s) to avoid
any conflict.
* (If moving from Alpha 3 to Alpha 4 the project settings have chagned.
Please reimport the project)


     4/ Example application
     ----------------------

The QpsExample directory contains a demo application which you can run by
yourself, inspect and/or copy/paste to get up and running faster. It displays
the raw output of QPS on a map, using Google map rendering APIs.

The application is an Eclipse project that you can import via:
  File > Import > Existing Projects into Workspace

You will have to manually import the QPS libraries into the project (step #1
above), and fill in your own customer and site settings (step #7 above).

Make sure the Google APIs for the API level you are targetting are installed.
Note that you will have to generate your own Google Maps API key in order to
run the application. Your own key should be added in res/layout/main.xml. If
you do not have one, you can easily create it by signing-up there:
http://code.google.com/android/maps-api-signup.html


     5/ Advanced features
     --------------------

     5.1/ Wi-Fi & GSM settings notifications

While the positioning service is running, notifications can be shown to the
user if the Wi-Fi is disabled, or if 3G is currently selected over GSM. By
default, the notifications will be disabled unless you provide your own
labels in res/values/strings.xml:

    <string name="qps_wifi_notification_title">
      Unable to get your position
    </string>
    <string name="qps_wifi_notification_content">
      Positioning requires Wi-Fi, tap to enable it
    </string>

    <string name="qps_gsm_notification_title">
      Improve the positioning accuracy
    </string>
    <string name="qps_gsm_notification_content">
      Select \"GSM only\" in \"Network mode\"
    </string>

Read more about this in the javadoc for AbstractRssiPositioningService.


     5.2/ DummyPositioningService

The API comes with a dummy implementation of a positioning service that can help
you during development by letting you fake positions and thus remove the need
for customer id, site id or any data connection during the development phase.

1. Follow the step-by-step for the actual implementation

2. Update the component providing positioning in AndroidManifest.xml:

     <service android:name="com.qubulus.qps.DummyPositioningService">

3. Add your mock locations to res/values/strings.xml, for example:

     <string name="dummy_map0">1</string>
     <string name="dummy_lat0">55.611501</string>
     <string name="dummy_lon0">13.010363</string>
     <string name="dummy_map1">2</string>
     <string name="dummy_lat1">55.611695</string>
     <string name="dummy_lon1">13.010420</string>
     <string name="dummy_delay">1000</string>

When you application is ready for field testing, simply switch back the android
service name to RemotePositioningService and remove the "dummy_*" values.

Read more about this in the javadoc for DummyPositioningService.


     6/ Changelog
     ------------

Alpha 4
* Ability to change settings in runtime:
    site id
    floor switch queue size
    floor switch queue tollerance
* Moved libs/ to QPSExample/libs and upated project settings

Alpha 3
* Improved stability using accelerometer readings.

Alpha 2
* Implementation: decreased jumpiness in the position estimates by sending/
  receiving more information to/from the server.
* API: Added SOURCE extra for com.qubulus.qps.Intents#*.
* API: Exposed PositioningManager#REQUEST_ACTION to clarify how the API
  chooses the implementation to delegate the requests to.

Alpha 1
* Initial public release.

