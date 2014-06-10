#\#gocd RPMBuild Task Plugin

## Building [![Build Status](https://snap-ci.com/sachinsudheendra/gocd_rpmbuild_task/branch/master/build_image)](https://snap-ci.com/sachinsudheendra/gocd_rpmbuild_task/branch/master)

Clone the repository and execute ```ant``` in the checkout directory. The built plugin will be in ```dist``` directory.

```bash
git clone https://github.com/sachinsudheendra/gocd_rpmbuild_task.git
cd gocd_rpmbuild_task
ant
```

## Usage

Drop ```gocd_rpmbuild_plugin.jar``` in ```/var/lib/go-server/plugins/external``` directory and restart the server.

```bash
cp gocd_rpmbuild_plugin.jar /var/lib/go-server/plugins/external
```
