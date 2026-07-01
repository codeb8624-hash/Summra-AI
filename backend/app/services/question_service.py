import logging
from typing import Dict, Any
from app.services.rag_service import rag_service
from app.services.ai_service import call_openrouter
from app.utils.markdown_cleaner import clean_markdown

logger = logging.getLogger(__name__)


class QuestionService:
    async def generate_content(self, document_id: str, task_type: str, language: str = "English") -> Dict[str, Any]:
        results = await rag_service.get_relevant_context("overall summary notes quiz flashcards", document_id)

        context_text = "\n\n".join([c['text'] for c in results])

        prompts = {
            "notes": "Generate comprehensive chapter notes and revision notes from the document.",
            "flashcards": "Generate flashcards with Front (Question) and Back (Answer) from the document.",
            "quiz": "Generate a quiz with MCQs, True/False, and Fill in the blanks from the document.",
            "important_questions": "Generate important exam questions (5 marks and 10 marks) from the document.",
            "explain": "Explain the document topics in a simple, easy to understand way like a teacher.",
            "explain_10": "Explain the document topics like I'm 10 years old.",
            "translate": f"Translate the core summary and key points of the document into {language}.",
        }

        task_prompt = prompts.get(task_type, "Summarize the document.")

        system_prompt = (
            "You are an educational assistant. Use the provided context to fulfill the user's request. "
            "Respond in a structured format using Markdown."
        )

        messages = [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": f"Context:\n{context_text}\n\nTask: {task_prompt}"},
        ]

        result = await call_openrouter(messages)

        if not result["success"]:
            logger.debug("Task OpenRouter error: %s", result.get("error"))
            return {"error": result.get("message", "Failed to generate content")}

        return {"content": clean_markdown(result["content"])}


question_service = QuestionService()
