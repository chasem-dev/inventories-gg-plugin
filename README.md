# inventories-gg-plugin

http://inventories.chasem.dev/

# Using dev.chasem GitHub Packages Maven Dependency

create a file at ~/.m2/settings.xml
Paste the following contents, and fill in username / password.

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
		  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
				  http://maven.apache.org/xsd/settings-1.0.0.xsd">
	<activeProfiles>
		<activeProfile>github</activeProfile>
	</activeProfiles>
	<profiles>
		<profile>
			<id>github</id>
			<repositories>
				<repository>
					<id>central</id>
					<url>https://repo1.maven.org/maven2</url>
				</repository>
				<repository>
					<id>github</id>
					<url>https://maven.pkg.github.com/Xwaffle1/inventories-core</url>
					<snapshots>
						<enabled>true</enabled>
					</snapshots>
				</repository>

			</repositories>
		</profile>
	</profiles>
	<servers>
		<server>
			<id>github</id>
			<username>USERNAME</username>
			<password>PASSWORD OR GITHUB PERSONAL TOKEN</password>
		</server>
	</servers>

</settings>
```