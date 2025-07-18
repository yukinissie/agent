# AWS EKS デプロイメント

このディレクトリには、agentシステムをAWS EKSにデプロイするためのInfrastructure as Code (IaC)が含まれています。

## 前提条件

- 適切な権限で設定されたAWS CLI
- Terraform >= 1.0
- kubectl
- Helm >= 3.12
- Docker
- GitHub ActionsのOIDCが設定されたGitHubリポジトリ

## 必要なAWS権限

あなたのAWSユーザー/ロールには以下の権限が必要です：
- EKSクラスター管理
- VPCとネットワークリソース
- ECRリポジトリ管理
- IAMロールとポリシー管理
- ノードグループ用のEC2インスタンス

## クイックスタート

### 1. Terraformバックエンドの設定

`terraform/versions.tf`を編集してS3バックエンドを設定します：

```hcl
backend "s3" {
  bucket = "your-terraform-state-bucket"
  key    = "agent/terraform.tfstate"
  region = "us-west-2"
}
```

### 2. 変数の設定

`terraform/terraform.tfvars`を作成：

```hcl
project_name = "agent"
environment = "prod"
aws_region = "us-west-2"

# オプション：デフォルト値の上書き
vpc_cidr = "10.0.0.0/16"
node_group_desired_size = 3
node_group_max_size = 10
```

### 3. インフラストラクチャのデプロイ

```bash
# 初期化とデプロイ
make init
make plan
make apply

# クラスターセットアップ（cert-managerをインストール）
make setup-cluster

# ALBコントローラーのインストール
make install-alb-controller

# アプリケーションのデプロイ
make deploy
```

## GitHub Actionsのセットアップ

### 1. OIDCの設定

GitHub Actions用のOIDCプロバイダーをAWSアカウントに作成：

```bash
# EKSクラスターからOIDC発行者URLを取得
aws eks describe-cluster --name agent-prod --query "cluster.identity.oidc.issuer" --output text

# このURLを使用してIAMでOIDCプロバイダーを作成
```

### 2. GitHubシークレットの作成

以下のシークレットをGitHubリポジトリに追加：

- `AWS_ROLE_ARN`: GitHub Actions用のIAMロールのARN

### 3. イメージURLの更新

ECRリポジトリが作成された後、本番環境のvaluesファイルを更新：

```bash
# TerraformアウトプットからECR URLを取得
terraform output example_api_ecr_repository_url
terraform output agent_api_ecr_repository_url

# valuesファイルを更新
# k8s/example-api/helm/values-prod.yaml
# k8s/agent-api/helm/values-prod.yaml
```

## アーキテクチャ

### インフラストラクチャコンポーネント

- **VPC**: パブリックサブネットとプライベートサブネットを持つマルチAZ VPC
- **EKSクラスター**: ワーカーノードを持つマネージドKubernetesクラスター
- **ECR**: 両方のAPI用のコンテナレジストリ
- **ALB**: イングレス用のApplication Load Balancer
- **IAM**: IRSA (IAM Roles for Service Accounts) 用のロールとポリシー

### ネットワークアーキテクチャ

```
Internet Gateway
       |
   Public Subnets (ALB)
       |
   Private Subnets (EKS Nodes)
       |
   NAT Gateways
```

### セキュリティ

- プライベートサブネット内のワーカーノード
- 最小限の必要なアクセスのセキュリティグループ
- Pod レベルのIAM権限のためのIRSA
- コンテナイメージ脆弱性スキャン

## 利用可能なコマンド

| コマンド | 説明 |
|---------|-------------|
| `make init` | Terraformの初期化 |
| `make plan` | Terraformの変更計画 |
| `make apply` | Terraformの変更適用 |
| `make destroy` | インフラストラクチャの破棄 |
| `make setup-cluster` | EKSクラスターのセットアップ (cert-manager) |
| `make install-alb-controller` | AWS Load Balancer Controllerのインストール |
| `make deploy` | アプリケーションのデプロイ |
| `make clean` | 全リソースのクリーンアップ |

## 監視とログ

### CloudWatchログ

EKSクラスターのログは自動的にCloudWatchに送信されます：
- APIサーバーログ
- 監査ログ
- 認証ログ
- コントローラーマネージャーログ
- スケジューラーログ

### ログアクセス

```bash
# クラスターログの表示
aws logs describe-log-groups --log-group-name-prefix "/aws/eks/agent-prod"

# アプリケーションログの表示
kubectl logs -f deployment/example-api
kubectl logs -f deployment/agent-api
```

## スケーリング

### Horizontal Pod Autoscaler (HPA)

両方のAPIはHPAで設定されています：

```bash
# HPAステータスの確認
kubectl get hpa

# 必要に応じて手動スケール
kubectl scale deployment example-api --replicas=5
```

### クラスターオートスケーラー

自動ノードスケーリング用のクラスターオートスケーラーの設定：

```bash
# クラスターオートスケーラーのインストール
kubectl apply -f https://raw.githubusercontent.com/kubernetes/autoscaler/master/cluster-autoscaler/cloudprovider/aws/examples/cluster-autoscaler-autodiscover.yaml
```

## トラブルシューティング

### よくある問題

1. **ALBコントローラーが動作しない**
   ```bash
   # ALBコントローラーのログ確認
   kubectl logs -n kube-system deployment/aws-load-balancer-controller
   
   # IRSA設定の確認
   kubectl describe sa aws-load-balancer-controller -n kube-system
   ```

2. **Podが起動しない**
   ```bash
   # Podイベントの確認
   kubectl describe pod <pod-name>
   
   # イメージがプルできるかの確認
   kubectl get events --sort-by=.metadata.creationTimestamp
   ```

3. **Ingressが動作しない**
   ```bash
   # Ingressステータスの確認
   kubectl get ingress
   kubectl describe ingress <ingress-name>
   
   # AWSコンソールでALBの確認
   aws elbv2 describe-load-balancers
   ```

### デバッグコマンド

```bash
# クラスターヘルスの確認
kubectl get nodes
kubectl get pods --all-namespaces

# ネットワークの確認
kubectl get svc
kubectl get ingress

# EKSクラスター情報の確認
aws eks describe-cluster --name agent-prod

# ノードグループの確認
aws eks describe-nodegroup --cluster-name agent-prod --nodegroup-name agent-prod-nodes
```

## コスト最適化

### スポットインスタンス

設定はコスト削減のためデフォルトでスポットインスタンスを使用：

```hcl
node_group_capacity_type = "SPOT"
```

### リソースリクエスト/制限

本番環境のvaluesには適切なリソースリクエストと制限が含まれています：

```yaml
resources:
  requests:
    cpu: 500m
    memory: 512Mi
  limits:
    cpu: 1000m
    memory: 1Gi
```

## クリーンアップ

全リソースを破棄するには：

```bash
make clean
```

これにより以下が実行されます：
1. Kubernetesアプリケーションの削除
2. ALBコントローラーの削除
3. cert-managerの削除
4. Terraformインフラストラクチャの破棄
5. Dockerイメージのクリーンアップ

## セキュリティベストプラクティス

1. **ネットワークセキュリティ**
   - プライベートサブネット内のワーカーノード
   - 最小限のアクセスのセキュリティグループ
   - AWSサービス用のVPCエンドポイント

2. **IAMセキュリティ**
   - インスタンスプロファイルの代わりにIRSAを使用
   - 最小限の必要な権限
   - 定期的なアクセスレビュー

3. **コンテナセキュリティ**
   - イメージ脆弱性スキャン
   - 非rootコンテナ
   - 読み取り専用ルートファイルシステム

4. **シークレット管理**
   - AWS Secrets Managerの使用
   - 定期的なシークレットローテーション
   - シークレットアクセスの監査

## サポート

問題や質問については：
1. トラブルシューティングセクションを確認
2. AWS EKSドキュメントをレビュー
3. GitHub Actionsログを確認
4. AWS権限とクォータを確認