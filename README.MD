Small example of payment system with in-memory storage.

**How to build**

On linux `./gradlew installDist`

On windows `gradlew.bat installDist`

**How to run**

Run scripts(sh and bat) located at `./build/install/trivial-banking/bin`

Arguments list available with `--help`

**Usage**

API is not exactly RESTful, but in my opinion it's better suites payment system.\
To create new accounts use POST to `/accounts`

`curl --header "Content-Type: application/json" --data '{"owner":"xyz","balance":1}' http://localhost:8080/accounts`

To get info/remove accounts use GET/DELETE to `/accounts/<id>`

`curl -X GET http://localhost:8080/accounts/100`

`curl -X DELETE http://localhost:8080/accounts/100`

To get accounts list use GET to `/accounts`

`curl -X GET http://localhost:8080/accounts`

To alter balance use POST to `/accounts/<id>/withdraw` and `/accounts/<id>/deposit`

`curl --data '20' http://localhost:8080/accounts/10/deposit`

To make a transfer between two accounts use `/transfer`

`curl --header "Content-Type: application/json" --data '{"sender":1,"receiver":2,"amount":10}' http://localhost:8080/transfer`

**Why sender/receiver instead of payer/payee?**

Because payer/payee is too similar, which tends to be error prone.
So I use slightly incorrect but more safe terms. 

**Why AccountRecord is so complicated?**

Concurrent programming is not an easy feat and really difficult to debug.
Incorporating synchronisation into types system makes it impossible to forget to release lock or accidentally alter record without taking lock.\
Read lock could be replaced with volatile field, but in this case it will be impossible to operate with consistent state without additional locks.

**Why use globalLock in ProcessingImpl?**

To be able to get consistent state.
This implementation could lead to significant performance impact if snapshot is requested often,
so if business logic requires to make a lot of snapshot requests consider to use another way to extract consistent state.

**Why no tests for controllers?**

For this project I decide to try another http library.
Unfortunately it does not provide test kit, so writing tests somewhat complicated.
To further worsen this problem you can't extract actual port if you bind to random one.
So the only choice was to bind on fixed port, which is bad practice.\
For this reasons I will not recommend to use `rapidoid` in production.\
Since controllers is trivial, eventually I decide to omit the tests.
