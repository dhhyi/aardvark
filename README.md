This is a tool for automatically transferring the time logged in [aTimeLogger](http://www.atimelogger.com) into your companies [Blue Ant](https://www.proventis.net/de/) project tracking tool.

# Requirements

- [Groovy](http://groovy-lang.org/)

# Get started

First (and regularly when your Bue Ant Tasks change) run the script for retrieving available Blue Ant tasks:

```bash
groovy get_BlueAnt_tasks.groovy <blue_ant_url>
```

When you see a login-screen, log in.

After that convert your aTimeLogger report to a intermediate common format:

```bash
groovy convert_aTimeLogger.groovy <report-date>.csv
```

Then you can start up the UI:

```bash
groovy aardvark_ui.groovy
```

After composing all mapping rules and successfully generating a report, save it and run the logging script:

```bash
groovy log_BlueAnt.groovy
```

# Supporting different formats

You need to have a (convenient) time logging app, that generates a report. Compose a script that transforms this report to the following CSV format:

```csv
dd.MM.yyyy;<comment>;HH:mm
```
