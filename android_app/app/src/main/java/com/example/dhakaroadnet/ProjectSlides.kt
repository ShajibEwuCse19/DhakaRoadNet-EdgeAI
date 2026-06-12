package com.example.dhakaroadnet

data class ProjectSlide(
    val imageResId: Int,
    val caption: String
)

object ProjectSlides {
    val slides = listOf(
        ProjectSlide(
            R.drawable.report_class_distribution,
            "Class distribution from the custom Dhaka road dataset."
        ),
        ProjectSlide(
            R.drawable.report_sample_visualization,
            "Sample annotated road scene used during dataset verification."
        ),
        ProjectSlide(
            R.drawable.report_loss_curves,
            "Training and validation loss curves from the YOLOv8n baseline."
        ),
        ProjectSlide(
            R.drawable.report_metrics_curves,
            "Precision, recall, and mAP curves recorded during training."
        ),
        ProjectSlide(
            R.drawable.report_confusion_matrix,
            "Normalized confusion matrix from model evaluation."
        ),
        ProjectSlide(
            R.drawable.report_validation_predictions,
            "Validation prediction grid from the trained detector."
        ),
        ProjectSlide(
            R.drawable.report_test_predictions,
            "Test prediction grid used for qualitative review."
        ),
        ProjectSlide(
            R.drawable.report_gradio_test,
            "Gradio demo test showing Python-side model deployment."
        )
    )
}
