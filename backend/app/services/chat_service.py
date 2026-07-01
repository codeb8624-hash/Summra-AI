import logging
from typing import List, Dict, Any
from app.services.rag_service import rag_service
from app.services.ai_service import call_openrouter
from app.utils.markdown_cleaner import clean_markdown

logger = logging.getLogger(__name__)


class ChatService:
    async def get_answer(self, document_id: str, question: str, history: List[Dict[str, str]] = None) -> Dict[str, Any]:
        contexts = await rag_service.get_relevant_context(question, document_id)

        if not contexts:
            return {
                "answer": "This document does not contain that information.",
                "sources": []
            }

        context_text = "\n\n".join([f"Source (Page {c['metadata']['pageNumber']}): {c['text']}" for c in contexts])

        system_prompt = (
            "You are an educational assistant. Answer ONLY from the provided document context. "
            "Never invent information. If the answer is not found in the context, say "
            "'This document does not contain that information.' "
            "Every answer should include Page Number citations in the text."
        )

        user_message = f"Document Context:\n{context_text}\n\nQuestion: {question}"

        messages = [{"role": "system", "content": system_prompt}]

        if history:
            messages.extend(history)

        messages.append({"role": "user", "content": user_message})

        result = await call_openrouter(messages)

        if not result["success"]:
            logger.debug("Chat OpenRouter error: %s", result.get("error"))
            return {
                "answer": "Sorry, I encountered an error while communicating with the AI service.",
                "sources": []
            }

        sources = list(set([f"Page {c['metadata']['pageNumber']}" for c in contexts]))

        return {
            "answer": clean_markdown(result["content"]),
            "sources": sources
        }


chat_service = ChatService()
