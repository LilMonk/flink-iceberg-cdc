FROM flink:1.16.0-scala_2.12-java8

# install python3: it has updated Python to 3.9 in Debian 11 and so install Python 3.7 from source
# it currently only supports Python 3.6, 3.7 and 3.8 in PyFlink officially.

RUN set -ex; \
    apt-get update && \
    apt-get install -y wget lzma liblzma-dev build-essential libncursesw5-dev libssl-dev libsqlite3-dev tk-dev libgdbm-dev libc6-dev libbz2-dev libffi-dev zlib1g-dev vim && \
    wget https://www.python.org/ftp/python/3.8.0/Python-3.8.0.tgz && \
    tar -xvf Python-3.8.0.tgz && \
    cd Python-3.8.0 && \
    ./configure --without-tests --enable-shared && \
    make -j4 && \
    make install && \
    ldconfig /usr/local/lib && \
    cd .. && rm -f Python-3.8.0.tgz && rm -rf Python-3.8.0 && \
    ln -s /usr/local/bin/python3 /usr/local/bin/python && \
    ln -s /usr/local/bin/pip3 /usr/local/bin/pip && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* && \
    python -m pip install --upgrade pip;


RUN mkdir -p /data
RUN mkdir -p /tmp/flink/logs/ && chmod a+w /tmp/flink/logs/

# install python dependencies.
RUN mkdir -p /python_dependencies
COPY ./requirements.txt /python_dependencies

# install flink and other python libs.
RUN pip install --upgrade pip && pip install -r /python_dependencies/requirements.txt

# copy required jars.
RUN mkdir -p /opt/flink/plugins/flink-s3-fs-hadoop
RUN cp /opt/flink/opt/flink-s3-fs-hadoop-1.16.0.jar /opt/flink/plugins/flink-s3-fs-hadoop

# Install all dependency jars
RUN wget -P /opt/flink/lib/ https://repo.maven.apache.org/maven2/org/apache/flink/flink-shaded-hadoop-2-uber/2.8.3-10.0/flink-shaded-hadoop-2-uber-2.8.3-10.0.jar
RUN wget -P /opt/flink/lib/ https://repo1.maven.org/maven2/org/apache/iceberg/iceberg-flink-runtime/0.12.1/iceberg-flink-runtime-0.12.1.jar
RUN wget -P /opt/flink/lib/ https://repo1.maven.org/maven2/com/ververica/flink-sql-connector-mysql-cdc/2.3.0/flink-sql-connector-mysql-cdc-2.3.0.jar
