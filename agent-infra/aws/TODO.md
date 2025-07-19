# AWS EKS デプロイメント TODO リスト

このドキュメントには、AWS EKSインフラストラクチャを正常にデプロイするために手動で設定する必要がある項目が含まれています。

## 🚀 新しい環境変数ベース設定

このプロジェクトは環境変数ベースの設定システムに更新されました。すべての設定は`.env`ファイルで一元管理され、面倒な手動設定が大幅に削減されました。

## 🔧 必須設定項目

### 1. 環境設定ファイルの作成と編集

**ファイル:** `agent-infra/aws/.env` (`.env.example`からコピーして作成)

```bash
# AWS設定  
AWS_REGION=us-west-2
AWS_ACCOUNT_ID=123456789012  # TODO: あなたのAWSアカウントIDに置き換え

# プロジェクト設定
PROJECT_NAME=agent
ENVIRONMENT=prod

# ドメイン設定
EXAMPLE_API_DOMAIN=example-api.yourdomain.com  # TODO: あなたのドメインに置き換え
AGENT_API_DOMAIN=agent-api.yourdomain.com      # TODO: あなたのドメインに置き換え

# Terraformバックエンド設定
TERRAFORM_STATE_BUCKET=your-terraform-state-bucket  # TODO: S3バケット名に置き換え
TERRAFORM_STATE_KEY=agent/terraform.tfstate
TERRAFORM_STATE_REGION=us-west-2

# EKS設定
CLUSTER_NAME=agent-prod
NODE_GROUP_DESIRED_SIZE=3
NODE_GROUP_MAX_SIZE=10
NODE_GROUP_MIN_SIZE=1
```

**必要な作業:**
- [ ] `.env.example`を`.env`にコピー
- [ ] `AWS_ACCOUNT_ID`をあなたの実際のAWSアカウントIDに置き換え
- [ ] `EXAMPLE_API_DOMAIN`と`AGENT_API_DOMAIN`をあなたの実際のドメインに置き換え
- [ ] `TERRAFORM_STATE_BUCKET`をあなたのS3バケット名に置き換え
- [ ] 必要に応じて他の設定値を調整

---

### 2. S3バケットの作成

**必要な作業:**
- [ ] Terraform状態ファイル用のS3バケットを作成

```bash
# S3バケットの作成
aws s3 mb s3://your-terraform-state-bucket --region us-west-2

# バージョニングの有効化（推奨）
aws s3api put-bucket-versioning \
  --bucket your-terraform-state-bucket \
  --versioning-configuration Status=Enabled
```

---

### 3. ドメイン名の設定

ドメインは`.env`ファイルで設定されるため、ファイルの手動編集は不要です。

**必要な作業:**
- [ ] ドメイン名を購入/取得
- [ ] `.env`ファイルの`EXAMPLE_API_DOMAIN`と`AGENT_API_DOMAIN`を実際のドメインに設定
- [ ] DNS設定（ALB作成後にCNAMEレコードを設定）
- [ ] SSL証明書（cert-managerが自動で処理）

**注意:** ドメインを持っていない場合は、README.mdの「クイックスタート」セクションを参照してLoadBalancerタイプのサービスを使用できます。

---

### 4. GitHubシークレットの設定（オプション）

GitHub Actionsを使用する場合のみ必要です。

**必要な作業:**
- [ ] AWS IAMでGitHub Actions用のOIDCプロバイダーを設定
- [ ] GitHub Actions用のIAMロールを作成
- [ ] GitHubリポジトリの設定で `AWS_ROLE_ARN` シークレットを追加
- [ ] ロールARNの値を設定

**注意:** ローカルでのデプロイのみの場合、この設定は不要です。

---

## 🛠️ オプション設定項目

### 5. ECR URLの手動設定（オプション）

ECR URLを`.env`ファイルに事前設定したい場合：

```bash
# Terraformを実行後、ECR URLを取得して.envに追加
echo "EXAMPLE_API_ECR_URL=$(cd terraform && terraform output -raw example_api_ecr_repository_url)" >> .env
echo "AGENT_API_ECR_URL=$(cd terraform && terraform output -raw agent_api_ecr_repository_url)" >> .env
```

**注意:** この設定は省略可能です。設定されていない場合、デプロイスクリプトが自動的にTerraformアウトプットから取得します。

---

## 📋 簡単セットアップ順序

環境変数ベース設定により、セットアップが大幅に簡素化されました：

### フェーズ1: 前提条件
- [ ] AWS CLI のインストールと設定
- [ ] Terraform のインストール (>= 1.0)
- [ ] kubectl のインストール
- [ ] Helm のインストール (>= 3.12)  
- [ ] Docker のインストール

### フェーズ2: 環境設定
1. [ ] `.env.example`を`.env`にコピー
2. [ ] `.env`ファイルを編集（AWS_ACCOUNT_ID、ドメイン、S3バケット名を設定）
3. [ ] Terraform状態用S3バケットの作成

### フェーズ3: インフラストラクチャデプロイ
4. [ ] `make init` の実行（`.env`から自動的に設定読み込み）
5. [ ] `make plan` の実行と確認
6. [ ] `make apply` の実行

### フェーズ4: Kubernetesセットアップ
7. [ ] `make setup-cluster` の実行
8. [ ] `make install-alb-controller` の実行

### フェーズ5: アプリケーションデプロイ
9. [ ] コンテナイメージのビルドとプッシュ（必要な場合）
10. [ ] `make deploy` の実行（ドメインとECR URLを自動設定）

### フェーズ6: DNS設定（ドメイン使用時のみ）
11. [ ] ALBの作成確認
12. [ ] DNS設定（CNAMEレコード）

## 🎯 変更点のまとめ

### ✅ 自動化された項目
- ✅ Terraformバックエンド設定（`.env`から自動読み込み）
- ✅ Terraform変数の設定（`.env`から自動読み込み）
- ✅ Helm valuesファイルの編集（環境変数で自動置換）
- ✅ ECR URLの取得と設定（デプロイ時に自動処理）

### ⚠️ 手動設定が必要な項目
- ⚠️ `.env`ファイルの初期設定
- ⚠️ S3バケットの作成
- ⚠️ ドメインの購入とDNS設定
- ⚠️ GitHub Actions設定（使用する場合）

---

## 🚀 超簡単クイックスタート

最小限の設定でテストする場合：

1. **環境設定:**
   ```bash
   cp .env.example .env
   # .envファイルでAWS_ACCOUNT_IDのみ変更、他はデフォルトのまま
   ```

2. **ローカルTerraformバックエンド使用:**
   ```bash
   # .envのTERRAFORM_STATE_BUCKETを空白に設定するとローカルバックエンドを使用
   TERRAFORM_STATE_BUCKET=
   ```

3. **ドメインなしでテスト:**
   ```bash
   # .envでドメイン設定をスキップ（LoadBalancer URLでアクセス）
   EXAMPLE_API_DOMAIN=
   AGENT_API_DOMAIN=
   ```

この設定で`make init && make apply && make setup-cluster && make deploy`を実行すれば最小設定でデプロイできます。

---

## 📞 サポート

設定で問題が発生した場合：

1. **ログの確認:**
   ```bash
   # Terraformログ
   terraform plan -detailed-exitcode
   
   # Kubernetesログ  
   kubectl get events --sort-by=.metadata.creationTimestamp
   kubectl logs -n kube-system deployment/aws-load-balancer-controller
   ```

2. **トラブルシューティング:**
   - README.mdのトラブルシューティングセクションを参照
   - AWS EKSドキュメントを確認
   - GitHub Actionsログを確認

3. **権限の確認:**
   ```bash
   # AWS権限の確認
   aws sts get-caller-identity
   aws eks describe-cluster --name agent-prod
   ```