# How to release the library

## Prepare your environment (first time only)

If it doesn't exist, create a secret keyring in `~/.gnupg/secring.gpg`:

```
gpg --keyring secring.gpg --export-secret-keys > ~/.gnupg/secring.gpg
```

Locate the GPG key that will be used to sign the artifacts:

```
$ gpg -K
pub   rsa2048 xxx [SC] [expire : xxx]
      xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
uid          [  ultime ] xxx
sub   rsa2048 xxx [E] [expire : xxx]
```

Copy the last 8 characters of the public key under `pub`.

Create or edit your global Gradle config, in `~/.gradle/gradle.properties` and
add the following properties:

```properties
signing.keyId=<last 8 chars of your GPG key>
signing.password=<password fot this GPG key>
signing.secretKeyRingFile=<absolute path to ~/.gnupg/secring.gpg>

sonatypeUsername=<your Sonatype JIRA login>
sonatypePassword=<your Sonatype JIRA password>
```

## Prepare the release

Adjust the properties `libraryVersion` and `antlr4Version` in the project's `gradle.properties`
if needed.

Build the artifact:

```
$ ./gradlew clean assemble
```

## Create the release on GitHub

Make sure everything is committed in your local Git repo, then create the tag:

```
$ git tag <version>
$ git push --tags 
```

Create a release on GitHub from this tag.

Bump the version in `gradle.properties`, commit and push.

## Publish the release on Maven Central

Publish the artifact on the Sonatype OSS repository:

```
$ ./gradlew publish
```

## More doc

[Sign and publish on Maven Central a Project with the new maven-publish Gradle plugin](https://medium.com/@nmauti/sign-and-publish-on-maven-central-a-project-with-the-new-maven-publish-gradle-plugin-22a72a4bfd4b)

[Automated Gradle project deployment to Sonatype OSS Repository](http://jedicoder.blogspot.com/2011/11/automated-gradle-project-deployment-to.html)
