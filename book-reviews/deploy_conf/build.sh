#!/bin/bash

set -o errexit


pushd "../"
    mvn package
    cp target/book-reviews.jar deploy_conf/book-reviews.jar
popd


#plain build -- no ratings
docker build -t "ruiwang14/istio-book-info-reviews-v1:1.0" -t ruiwang14/istio-book-info-reviews-v1:latest --build-arg service_version=v1 .
docker push "ruiwang14/istio-book-info-reviews-v1:1.0"
#with ratings black stars
docker build -t "ruiwang14/istio-book-info-reviews-v2:1.0" -t ruiwang14/istio-book-info-reviews-v2:latest --build-arg service_version=v2 \
   --build-arg enable_ratings=true .
docker push "ruiwang14/istio-book-info-reviews-v2:1.0"
#with ratings red stars
docker build -t "ruiwang14/istio-book-info-reviews-v3:1.0" -t ruiwang14/istio-book-info-reviews-v3:latest --build-arg service_version=v3 \
   --build-arg enable_ratings=true --build-arg star_color=red .
docker push "ruiwang14/istio-book-info-reviews-v3:1.0"
