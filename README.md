![2Paths Solutions Ltd.](images/2Paths-logo.png)
![NEP](images/nep-logo.png)

DHIS2Connector
==============

The **DHIS 2 Connector** is an open-sourced Third Party [Grails plugin](http://docs.grails.org/latest/guide/plugins.html) 
to facilitate the interaction with the DHIS 2 API via a Grails application. 

This plugin was developed in parallel with the **NEP Custom Data Import Application** but provides
more generic DHIS 2 API functionality that can be used more easily in other Grails applications.

The **DHIS 2 Connector** is open source software licensed under the liberal
[BSD license](http://www.linfo.org/bsdlicense.html)
and is free for everyone to install and use.

Please see the [License for the DHIS2Connector](LICENSE.md)

This codebase was developed in partnership with [2Paths Solutions Ltd.](https://www.2paths.com) and [Johns Hopkins 
University (JHU)](https://www.jhu.edu) and was finalized for the purposes of the project on July 31, 2017. 
If you have questions about this project, please contact the 2Paths Development Team at 
[nep-os@2paths.com](mailto:nep-os@2paths.com) or Emily Wilson at JHU at [ewilso28@jhu.edu](mailto:ewilso28@jhu.edu). 

Versions
--------

The DHIS2Connector has been developed against DHIS 2 versions 2.24 and 2.25. This github repository has the following
corresponding branches:

- Connector-v2.24
- Connector-v2.25

Please ensure you use the appropriate branch for the version of DHIS 2 that you are connecting to.

Development 
-----------

Developing on the **DHIS 2 Connector** codebase involves straightforward Groovy/Grails programming. 

### Installing the DHIS2Connector Grails Plugin into a Grails application

Clone this repository, ensuring you are using the correct DHIS 2 version (2.24 or 2.25).
Navigate to the DHIS2Connector plugin folder and run the following commands:

```bash
grails clean
grails compile
grails maven-install
```

This will install the plugin into your local maven repository and allow it to be referenced by a Grails application 
(eg: **NEP Custom Data Import App**). To use the plugin in your Grails application, you will reference it in the 
BuildConfig.groovy file within your Grails application.

Eg:

```groovy
    plugins {

        // DHIS2 Connector
        compile ":dhis-2-connector:2.24.5"
    }

```

To update the version of the plugin, edit the version value in the following file:

```bash
/dhis2-connector/DHIS2Connector/DHIS2ConnectorGrailsPlugin.groovy
```

You will need to update the plugins section of BuildConfig.groovy to use this new version and also refresh your 
dependencies in your Grails project to pick up the plugin.

Without updating the version, you may encounter issues with picking up a new version of the plugin when it has changed.
This may be resolved by updating the version number in application.properties of the plugin when there are code changes.  
Then update the version referenced in the BuildConfig.groovy file of the app using the plugin.

Coding DHIS 2 Upgrades
----------------------
When upgrading the codebase to be compatible with a new version of DHIS 2, development effort will vary depending mainly
upon the scope of changes made within DHIS 2, specifically in regards to the WEB API.

Your first step will be to review the upgrade and release notes supplied by the DHIS 2 team on the 
[DHIS 2 website](https://www.dhis2.org/).

Note any documented changes to the WEB API and update the codebase in a new branch for the version accordingly. The 
majority of the changes will likely need to be made directly in the DHIS2Connector Grails Plugin.

You will need to be mindful of:
1. Domain model changes
    * EG: in the 2.25 upgrade, the association between dataSet and dataElement was changed and required rewriting of 
    related portions of the application, including SQL View definitions. 
2. WEB API Request changes
3. WEB API Response changes

Our experience has been that these changes are not typically fully documented, so a codebase version upgrade often
involves comprehensive testing of the entire system and discovering differences / issues on a trial-and-error basis.

Contributing
------------

If you are interested in contributing to this project, please contact the 2Paths Development Team at 
[nep-os@2paths.com](mailto:nep-os@2paths.com). 

Contact Us
----------

Emily Wilson   
Research Associate  
NEP Program  
[Johns Hopkins University](https://www.jhu.edu)   
[ewilso28@jhu.edu](mailto:ewilso28@jhu.edu)


2Paths Development Team  
[2Paths Solutions Ltd.](https://www.2paths.com)  
[nep-os@2paths.com](mailto:nep-os@2paths.com)
