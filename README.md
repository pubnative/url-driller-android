![driller icon](url-driller.png)

[![Circle CI](https://circleci.com/gh/pubnative/url-driller/tree/master.svg?style=shield)](https://circleci.com/gh/pubnative/url-driller/tree/master)

# url-driller


This repository contains a tool to navigate deep into an URL redirection and open it in background.

## Install

###jCenter

Add the following line to your build.gradle file

`compile "net.pubnative:url_driller:1.2.1"`

###Manual

You can always download this repository and include it as a module in your project

## Usage

Create your driller and drill the url with it

```java
URLDriller driller = new URLDriller();
driller.drill(context, "<URL>");
```

#### Advanced

If you want to follow the driller behaviour, set up a valid listener before drilling and it will callback with the drilling steps.

```java
driller.setListener(new URLDriller.Listener() {

    @Override
    public void onURLDrillerStart(String url) {
        // Callback when drilling starts            
    }

    @Override
    public void onURLDrillerRedirect(String url) {
        // Callback when drilling followed a redirect
    }

    @Override
    public void onURLDrillerFinish(String url) {
        // Callback when drilling is over
    }

    @Override
    public void onURLDrillerFail(String url, Exception exception) {
        // Callback when drilling fails
    }
});
```
