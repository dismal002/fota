# FOTA Application Configuration Guide
This guide outlines the system properties and code-level configurations that partners can use to customize the FOTA (Firmware Over-The-Air) application for their specific hardware and regional requirements.

## 1. System Configuration (build.prop)

The FOTA app reads several `ro.fota.*` properties from the system's `build.prop`. These can be set during the Android build process.

| Property Key | Default Value | Description |
| :--- | :--- | :--- |
| `ro.fota.oem` | `Build.PRODUCT` | The project identifier used for update queries. |
| `ro.fota.version` | `Build.ID` | The current firmware version string. |
| `ro.fota.language` | (System Locale) | Overrides the default locale for reporting. |
| `ro.fota.platform` | `unknownPlatform` | The hardware platform (e.g., MT6737). |
| `ro.fota.type` | `phone` | Device type (`phone`, `pad`, `tv`, `box`). |
| `ro.fota.cycle` | `1` | Update check cycle (in days). |
| `ro.fota.auto.wifi` | `1` | `1` to enable automatic download over Wi-Fi. |
| `ro.fota.wifi.only` | `0` | `1` to restrict all FOTA traffic to Wi-Fi only. |
| `ro.fota.activate` | `15` | Delay (in minutes) after boot before the first update check. |
| `ro.fota.googleota` | `0` | `1` if the device uses Google OTA services in tandem. |
| `ro.fota.fmsuccess` | `0` | `1` to enable reporting of full-mount success. |
| `ro.fota.display` | `0` | UI display mode flag. |

## 2. Configuring Custom OTA Endpoints

To point the application to your own OTA infrastructure, you must modify the `ServerApi.java` class before building the APK.

### Centralized Endpoint Management
Modify `ServerApi.java` to update your domains:

```java
public class ServerApi {
    // Primary update check domain
    public static final String PRIMARY_DOMAIN = "https://your-ota-server.com";
    
    // Secondary/Failover domain
    public static final String SECONDARY_DOMAIN = "https://backup-ota-server.com";
    
    // Domain used for analytics and error reporting
    public static final String REPORT_DOMAIN = "https://your-report-server.com";
    
    // Legacy support (if applicable)
    public static final String LEGACY_REBOOT_DOMAIN = "http://your-legacy-server.com";
    
    // Path structure on your server
    public static final String API_PATH = "/your-path/";
}
```

### Protocol Note
It is **strongly recommended** to use `https://` for all production domains to prevent Man-in-the-Middle (MitM) attacks.

## 3. Customizing UI and Assets

- **App Name/Icon**: Modify `app/src/main/res/values/strings.xml` (`app_name`) and replace the icons in `app/src/main/res/mipmap-*`.
- **Default Check URL**: The application will automatically initialize its `check_url` preference using `ServerApi.LEGACY_REBOOT_DOMAIN` on the first run. To change the initial default, ensure `LEGACY_REBOOT_DOMAIN` is set correctly in `ServerApi.java`.

## 4. Privacy and Consent

If your region requires explicit user consent for data collection (IMEI/MAC), ensure that the `com.foss.privacypolicy` application is included in your system image. The FOTA app will automatically detect this app and respect the user's rejection status.
