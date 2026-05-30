from fastapi import FastAPI
from pydantic import BaseModel
from sentence_transformers import SentenceTransformer
import chromadb

app = FastAPI()

model = None
chroma_client = None
collection = None

@app.on_event("startup")
async def startup_event():
    global model, chroma_client, collection
    print("Loading sentence transformer model...")
    model = SentenceTransformer("all-MiniLM-L6-v2")
    print("Model loaded!")
    chroma_client = chromadb.HttpClient(host="localhost", port=8000)
    collection = chroma_client.get_or_create_collection("memora-embeddings")
    print("ChromaDB connected!")

class EmbedRequest(BaseModel):
    contentItemId: str
    text: str
    userId: str

class SearchRequest(BaseModel):
    queryText: str
    userId: str
    topK: int = 5

@app.post("/embed")
async def embed(request: EmbedRequest):
    embedding = model.encode(request.text).tolist()
    vector_id = f"{request.userId}:{request.contentItemId}"
    collection.add(
        ids=[vector_id],
        embeddings=[embedding],
        documents=[request.text],
        metadatas=[{"userId": request.userId}]
    )
    return {"vectorId": vector_id}

@app.post("/search")
async def search(request: SearchRequest):
    embedding = model.encode(request.queryText).tolist()
    results = collection.query(
        query_embeddings=[embedding],
        n_results=request.topK,
        where={"userId": request.userId}
    )
    output = []
    for doc_id, distance in zip(results["ids"][0], results["distances"][0]):
        content_item_id = doc_id.replace(f"{request.userId}:", "")
        output.append({
            "contentItemId": content_item_id,
            "similarityScore": 1 - distance
        })
    return output

@app.get("/health")
async def health():
    return {"status": "UP"}