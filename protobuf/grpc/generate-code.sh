#!/bin/bash

python3 -m grpc_tools.protoc -I/home/ace/github/xwatcher/protobuf/  \
  --python_out=/home/ace/github/xwatcher/protobuf/grpc/src/python   \
  --grpc_python_out=/home/ace/github/xwatcher/protobuf/grpc/src/python  \
  /home/ace/github/xwatcher/protobuf/grpc/TraceGraphAnalysis.proto

protoc --plugin=protoc-gen-grpc-java \
  --grpc-java_out="/home/ace/github/xwatcher/protobuf/grpc/src/java/TraceGraphAnalysis.java"   \
  --proto_path="/home/ace/github/xwatcher/protobuf/"  \
  "/home/ace/github/xwatcher/protobuf/grpc/TraceGraphAnalysis.proto"