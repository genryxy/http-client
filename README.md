<img src="https://www.artipie.com/logo.svg" width="64px" height="64px"/>

[![Maven build](https://github.com/artipie/http-client/workflows/Maven%20Build/badge.svg)](https://github.com/artipie/http-client/actions?query=workflow%3A%22Maven+Build%22)
[![PDD status](http://www.0pdd.com/svg?name=artipie/http-client)](http://www.0pdd.com/p?name=artipie/http-client)
[![License](https://img.shields.io/github/license/artipie/http-client.svg?style=flat-square)](https://github.com/artipie/http-client/blob/master/LICENSE)

Artipie HTTP client implementation.

To install add this dependency to `pom.xml` file:
```xml
<dependency>
  <groupId>com.artipie</groupId>
  <artifactId>http-client</artifactId>
  <version><!-- use latest version --></version>
</dependency>
```

Artipie HTTP module provides HTTP abstractions. Client module targeted to implement
HTTP client on top of these abstractions.

- `ClientSlice` - is a `Slice` that sends request to remote TCP endpoint and returns
`Response`. In that sense it is similar to an HTTP connection.
- `ClientSlices` - collection of slices that pools resources
and builds `ClientSlice` by specified protocol (HTTP or HTTPS), host and port.

`ClientSlices` is entry point for usage of this module:

```java
ClientSlices slices = new JettyClientSlices();
Slice slice = slices.slice("artipie.com", 80);
Response response = slice.request(
  "GET /index.html HTTP/1.1",
  Headers.EMPTY,
  Flowable.empty()
);
response.send(
  (status, headers, body) -> {
    // handle recieved data
  }
);
``` 

## How to contribute

Fork repository, make changes, send us a pull request. We will review
your changes and apply them to the `master` branch shortly, provided
they don't violate our quality standards. To avoid frustration, before
sending us your pull request please run full Maven build:

```
$ mvn clean install -Pqulice
```

To avoid build errors use Maven 3.3+.
