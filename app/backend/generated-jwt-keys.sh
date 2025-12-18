#!/bin/bash

# 상대 경로 기준 키 저장 위치
MAIN_KEY_DIR="src/main/resources/keys"
TEST_KEY_DIR="src/test/resources/keys"
MAIN_PRIVATE_KEY="${MAIN_KEY_DIR}/jwtRS256.key"
MAIN_PUBLIC_KEY="${MAIN_KEY_DIR}/jwtRS256.key.pub"
TEST_PRIVATE_KEY="${TEST_KEY_DIR}/jwtRS256.key"
TEST_PUBLIC_KEY="${TEST_KEY_DIR}/jwtRS256.key.pub"

generate_key_pair() {
  local private_key=$1
  local public_key=$2
  local target_dir
  local temp_key="${private_key}.tmp"

  target_dir=$(dirname "${private_key}")

  mkdir -p "${target_dir}"

  echo "Generating RSA private key (PKCS#1) at ${temp_key}..."
  openssl genrsa -out "${temp_key}" 2048

  echo "Converting to PKCS#8 format at ${private_key}..."
  openssl pkcs8 -topk8 -inform PEM -outform PEM -in "${temp_key}" -out "${private_key}" -nocrypt

  echo "Extracting public key to ${public_key}..."
  openssl rsa -in "${private_key}" -pubout -out "${public_key}"

  # 임시 파일 삭제
  rm -f "${temp_key}"

  chmod 600 "${private_key}"
  chmod 644 "${public_key}"

  echo "Successfully generated PKCS#8 key pair in ${target_dir}"
}

generate_key_pair "${MAIN_PRIVATE_KEY}" "${MAIN_PUBLIC_KEY}"
generate_key_pair "${TEST_PRIVATE_KEY}" "${TEST_PUBLIC_KEY}"

echo ""
echo "========================================="
echo "RSA key pairs generated successfully!"
echo "========================================="
echo "Format: PKCS#8 (BEGIN PRIVATE KEY)"
echo ""
echo "Main  - Private: ${MAIN_PRIVATE_KEY}"
echo "Main  - Public : ${MAIN_PUBLIC_KEY}"
echo "Test  - Private: ${TEST_PRIVATE_KEY}"
echo "Test  - Public : ${TEST_PUBLIC_KEY}"
