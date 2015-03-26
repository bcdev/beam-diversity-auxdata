#!/bin/bash

RUSER=marcoz
RHOST=node73
RDIR=/home/cvop/diversity-lakes-inst

for fname in $(find . -type f)
do
  if [[ "${fname}" == "./diversity-inst.iml" || "${fname}" == "./compare_with_remote.bash" || "${fname}" == "./my" ]]
  then
      continue
  fi
  #echo "=== ${fname} ==="
  #ls -l ${fname}
  #ssh ${RUSER}@${RHOST} "ls -l ${RDIR}/${fname}"
  diff -u --label "${RHOST}:${RDIR}/${fname}" <(ssh ${RUSER}@${RHOST} "if [ -f ${RDIR}/${fname} ]; then cat ${RDIR}/${fname}; fi") ${fname}  | colordiff
done