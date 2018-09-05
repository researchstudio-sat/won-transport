#!/bin/bash

set -e

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

cp -pf $DIR/../won-taxi-bot/target/taxi-bot.jar $DIR/taxi-bot/taxi-bot.jar

rm -rf $DIR/taxi-bot/conf
cp -rfp $DIR/../conf $DIR/taxi-bot/conf

rm -rf $DIR/taxi-bot/factory-needs/
cp -rfp $DIR/../won-taxi-bot/src/main/resources/FactoryNeeds $DIR/taxi-bot/factory-needs

$DIR/compose-helper.sh up -d --build