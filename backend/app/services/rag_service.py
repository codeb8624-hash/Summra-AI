import logging
import uuid
from typing import List, Dict, Any
from app.services.embedding_service import embedding_service
from app.services.vector_service import vector_service
from app.services.document_service import chunk_for_rag

logger = logging.getLogger(__name__)

class RAGService:
    async def process_document(self, text: str, document_name: str) -> str:
        document_id = str(uuid.uuid4())
        logger.info(f"Processing document {document_name} with ID {document_id}")

        # Chunk text for RAG
        chunks = chunk_for_rag(text)

        embeddings = embedding_service.generate_embeddings(chunks)

        metadatas = []
        for i in range(len(chunks)):
            metadatas.append({
                "documentId": document_id,
                "documentName": document_name,
                "chunkId": i,
                "pageNumber": i // 4 + 1, # Improved heuristic
            })

        vector_service.add_documents(
            document_id=document_id,
            texts=chunks,
            embeddings=embeddings,
            metadatas=metadatas
        )

        return document_id

    async def get_relevant_context(self, question: str, document_id: str) -> List[Dict[str, Any]]:
        query_embedding = embedding_service.generate_embedding(question)
        results = vector_service.query(query_embedding, n_results=5, document_id=document_id)

        contexts = []
        if results and "documents" in results and results["documents"]:
            for i in range(len(results["documents"][0])):
                contexts.append({
                    "text": results["documents"][0][i],
                    "metadata": results["metadatas"][0][i]
                })
        return contexts

# Singleton instance
rag_service = RAGService()
