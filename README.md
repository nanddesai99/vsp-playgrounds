# MATSim VSP playgrounds

[![Build Status](https://travis-ci.org/matsim-vsp/vsp-playgrounds.svg?branch=master)](https://travis-ci.org/matsim-vsp/vsp-playgrounds)
[![Packagecloud Repository](https://img.shields.io/badge/java-packagecloud.io-844fec.svg)](https://packagecloud.io/matsim-vsp/vsp-playgrounds/)

This repository contains MATSim related code of the VSP group at TU Berlin.

CI is run on [travis](https://travis-ci.org/matsim-vsp/vsp-playgrounds) and (snapshot) jars are deployed to [packagecloud](https://packagecloud.io/matsim-vsp/vsp-playgrounds/)

Please note, that this code is not designated stable.

For the main MATSim project, see https://github.com/matsim-org

## Use in external project

To use vsp-playgrounds as a dependency in an external maven project update the latter project's `pom.xml` in the following way:

1. Add the packagecloud repository to the `repositories` section:

```
<repositories>
	<repository>
		<id>matsim-vsp-vsp-playgrounds</id>
		<url>https://packagecloud.io/matsim-vsp/vsp-playgrounds/maven2</url>
	</repository>
</repositories>
```

2. Add dependency to the selected playgrounds in the `dependencies` section: 

```
<dependencies>
	<dependency>
		<groupId>org.matsim.vsp.vsp-playgrounds</groupId>
		<artifactId>$playground_name$</artifactId>
		<version>0.11.0-SNAPSHOT</version>
	</dependency>
</dependencies>
```
