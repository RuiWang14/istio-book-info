# istio-book-info
rewrite review service to spring boot.
if you meet `com.ibm.ws.kernel.boot.LaunchException` with review service,
hope this can help.

## use:
just replace `istio/examples-bookinfo-reviews-vX:last` with
`ruiwang14/istio-book-info-reviews-vX:last` (X can be 1, 2, 3).
or just kube apply the yaml directly.
```
kubectl apply -f ./kube/deploy.yaml
```

## to build & push images

``` bash
cd ./book-reviews/deploy_conf
./build.sh
```

