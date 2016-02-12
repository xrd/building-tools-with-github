
To run the example:

```
$ npm install
$ PROBOT_API_TOKEN=myslackapitoken PROBOT_SECRET=mysecret ./script/hubot
```

To run tests:

```
$ npm install
$ PROBOT_SECRET=XYZABC \
PROBOT_API_TOKEN=926a701550d4dfae93250dbdc068cce887531 \
node_modules/jasmine-node/bin/jasmine-node --coffee \
spec/pr-delegator.spec.coffee 
```

