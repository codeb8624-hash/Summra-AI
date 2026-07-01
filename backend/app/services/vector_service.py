import logging
import chromadb
from chromadb.config import Settings
import os
from typing import List, Dict, Any

logger = logging.getLogger(__name__)

class VectorService:
    def __init__(self):
        self.persist_directory = os.path.join(os.getcwd(), "chroma_db")
        os.makedirs(self.persist_directory, exist_ok=True)

        self.client = chromadb.PersistentClient(path=self.persist_directory)
        self.collection_name = "summra_documents"
        self.collection = self.client.get_or_create_collection(name=self.collection_name)
        logger.info(f"Vector search initialized at {self.persist_directory}")

    def add_documents(
        self,
        document_id: str,
        texts: List[str],
        embeddings: List[List[float]],
        metadatas: List[Dict[str, Any]]
    ):
        ids = [f"{document_id}_{i}" for i in range(len(texts))]
        self.collection.add(
            ids=ids,
            embeddings=embeddings,
            metadatas=metadatas,
            documents=texts
        )
        logger.info(f"Added {len(texts)} chunks for document {document_id}")

    def query(self, query_embedding: List[float], n_results: int = 5, document_id: str = None) -> Dict[str, Any]:
        where = {"documentId": document_id} if document_id else None
        results = self.collection.query(
            query_embeddings=[query_embedding],
            n_results=n_results,
            where=where
        )
        return results

# Singleton instance
vector_service = VectorService()
