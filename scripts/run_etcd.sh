#!/bin/bash

if [[ -z "${ETCD_VERSION}" ]]; then
  ETCD_VERSION="v3.1.8"
fi
OS=`uname`

if [[ ${OS} =~ ^Darwin ]]; then
  BINOS="darwin"
  SUFFIX=".zip"
elif  [[ ${OS} =~ ^Linux ]]; then
  BINOS="linux"
  SUFFIX=".tar.gz"
else
  echo "Unsupported OS: ${OS}"
  exit 1
fi

# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

PRGDIR=`dirname "$PRG"`

cd "${PRGDIR}"
cd ..

DIRNAME="etcd-${ETCD_VERSION}-${BINOS}-amd64"
BALL="${DIRNAME}${SUFFIX}"

mkdir -p external
cd external

if [[ ! -d "${DIRNAME}" ]]; then
  curl -L https://github.com/coreos/etcd/releases/download/${ETCD_VERSION}/${BALL} -o ${BALL}
  tar xzvf ${BALL}
else
  echo "skip to download since ${BALL} exists."
  echo "clean before start..."
  rm -rf "${DIRNAME}/infra1.etcd"
  rm -rf "${DIRNAME}/infra2.etcd"
  rm -rf "${DIRNAME}/infra3.etcd"
  rm -f "${DIRNAME}/nohup.out"
fi

cd ${DIRNAME}

./etcd --version

nohup bash -c './etcd --name infra1 --listen-client-urls http://127.0.0.1:2379 --advertise-client-urls http://127.0.0.1:2379 --listen-peer-urls http://127.0.0.1:12380 --initial-advertise-peer-urls http://127.0.0.1:12380 --initial-cluster-token etcd-cluster-1 --initial-cluster 'infra1=http://127.0.0.1:12380,infra2=http://127.0.0.1:22380,infra3=http://127.0.0.1:32380' --initial-cluster-state new --enable-pprof&'

nohup bash -c './etcd --name infra2 --listen-client-urls http://127.0.0.1:22379 --advertise-client-urls http://127.0.0.1:22379 --listen-peer-urls http://127.0.0.1:22380 --initial-advertise-peer-urls http://127.0.0.1:22380 --initial-cluster-token etcd-cluster-1 --initial-cluster 'infra1=http://127.0.0.1:12380,infra2=http://127.0.0.1:22380,infra3=http://127.0.0.1:32380' --initial-cluster-state new --enable-pprof&'

nohup bash -c './etcd --name infra3 --listen-client-urls http://127.0.0.1:32379 --advertise-client-urls http://127.0.0.1:32379 --listen-peer-urls http://127.0.0.1:32380 --initial-advertise-peer-urls http://127.0.0.1:32380 --initial-cluster-token etcd-cluster-1 --initial-cluster 'infra1=http://127.0.0.1:12380,infra2=http://127.0.0.1:22380,infra3=http://127.0.0.1:32380' --initial-cluster-state new --enable-pprof&'
