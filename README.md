_Thoughtworks Go_ - _RapidDeploy_ plugins 
==============

Plugins for an integration between _Thoughtworks Go_ and _MidVision RapidDeploy_.

## Dependencies
* The [_RapidDeploy_ connector](https://github.com/MidVision/rapiddeploy-connector) project.
  * It is all set up in the _POM_ file so there's no need to configure any extra thing.

## Download binaries
* You can find the packaged plugins [here](http://www.download.midvision.com/content/repositories/Community_release/com/midvision/plugins/go/).

## Data dictionary
* You can pass data dictionary items to a RapidDeploy deployment by using environment variables in GoCD. These variables keys must be preceded and ended by the "@@" string, for example:

```
@@echoMessageA@@ = Hello world!
```