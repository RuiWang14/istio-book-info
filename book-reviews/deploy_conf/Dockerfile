FROM    registry.cn-qingdao.aliyuncs.com/focus_data/jdk1.8u181:1.0
MAINTAINER     "chengruiwang213807@sohu-inc.com"

COPY book-reviews.jar /home/project/book-reviews.jar

ENV SERVERDIRNAME reviews
ARG service_version
ARG enable_ratings
ARG star_color
ENV SERVICE_VERSION ${service_version:-v1}
ENV ENABLE_RATINGS ${enable_ratings:-false}
ENV STAR_COLOR ${star_color:-black}

COPY start.sh /home/project/start.sh

RUN chmod 777 /home/project/start.sh

CMD /home/project/start.sh

