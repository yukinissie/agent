use axum::{extract::Path, http::StatusCode, response::Json, routing::get, Router};
use serde::{Deserialize, Serialize};
use tower::ServiceBuilder;

#[derive(Serialize, Deserialize)]
struct User {
    id: u32,
    name: String,
    email: String,
}

#[derive(Serialize)]
struct HealthResponse {
    status: String,
    message: String,
}

async fn health() -> Json<HealthResponse> {
    Json(HealthResponse {
        status: "ok".to_string(),
        message: "API is running".to_string(),
    })
}

async fn get_users() -> Json<Vec<User>> {
    let users = vec![
        User {
            id: 1,
            name: "Alice".to_string(),
            email: "alice@example.com".to_string(),
        },
        User {
            id: 2,
            name: "Bob".to_string(),
            email: "bob@example.com".to_string(),
        },
    ];
    Json(users)
}

async fn get_user(Path(id): Path<u32>) -> Result<Json<User>, StatusCode> {
    let user = User {
        id,
        name: format!("User {}", id),
        email: format!("user{}@example.com", id),
    };
    Ok(Json(user))
}

async fn create_user(Json(payload): Json<User>) -> (StatusCode, Json<User>) {
    (StatusCode::CREATED, Json(payload))
}

#[tokio::main]
async fn main() {
    let app = Router::new()
        .route("/health", get(health))
        .route("/users", get(get_users).post(create_user))
        .route("/users/:id", get(get_user))
        .layer(ServiceBuilder::new());

    let listener = tokio::net::TcpListener::bind("0.0.0.0:3000").await.unwrap();

    println!("Server running on http://0.0.0.0:3000");

    axum::serve(listener, app).await.unwrap();
}
