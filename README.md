# akka-stream-sse-example
typed actor を使ったSSE(Server Sent Events)

akka Receptionist(akka cluster) を使って、複数Play Framework サーバー間のメッセージをSSEする

## How to run
Start a Play app in the first terminal window:

```bash
sbt 'run -Dhttp.port=9000 -Dakka.remote.artery.canonical.port=2551'
```

And open [http://localhost:9000/](http://localhost:9000/).

After that start another Play app in the second terminal window:

```bash
sbt 'run -Dhttp.port=9001 -Dakka.remote.artery.canonical.port=2552'
```

And open [http://localhost:9001/](http://localhost:9001/).

## How to test
```bash
sbt test
```