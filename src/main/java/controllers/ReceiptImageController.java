package controllers;

import api.ReceiptSuggestionResponse;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.Collections;
import java.util.ArrayList;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import org.hibernate.validator.constraints.NotEmpty;

import static java.lang.System.out;

@Path("/images")
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.APPLICATION_JSON)
public class ReceiptImageController {
    private final AnnotateImageRequest.Builder requestBuilder;

    public ReceiptImageController() {
        // DOCUMENT_TEXT_DETECTION is not the best or only OCR method available
        Feature ocrFeature = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
        this.requestBuilder = AnnotateImageRequest.newBuilder().addFeatures(ocrFeature);

    }

    public boolean isNumeric(String s) {  
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");  
    }
    /**
     * This borrows heavily from the Google Vision API Docs.  See:
     * https://cloud.google.com/vision/docs/detecting-fulltext
     *
     * YOU SHOULD MODIFY THIS METHOD TO RETURN A ReceiptSuggestionResponse:
     *
     * public class ReceiptSuggestionResponse {
     *     String merchantName;
     *     String amount;
     * }
     */
    @POST
    public ReceiptSuggestionResponse parseReceipt(@NotEmpty String base64EncodedImage) throws Exception {
        // System.out.println(base64EncodedImage);
        Image img = Image.newBuilder().setContent(ByteString.copyFrom(Base64.getDecoder().decode(base64EncodedImage))).build();
        AnnotateImageRequest request = this.requestBuilder.setImage(img).build();

        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            System.out.println("google created the client");
            BatchAnnotateImagesResponse responses = client.batchAnnotateImages(Collections.singletonList(request));
            AnnotateImageResponse res = responses.getResponses(0);

            String merchantName = null;
            BigDecimal amount = null;

            System.out.println("iterating to get some meta data");
            // Your Algo Here!!
            // Sort text annotations by bounding polygon.  Top-most non-decimal text is the merchant
            // bottom-most decimal text is the total amount

            ArrayList<String> amounts = new ArrayList<String>();
            ArrayList<String> merchants = new ArrayList<String>();

            ArrayList<Integer> xs = new ArrayList<Integer>();
            ArrayList<Integer> ys = new ArrayList<Integer>();
            String cropCoordStr = "";

            for (EntityAnnotation annotation : res.getTextAnnotationsList()) {
                System.out.println();
                // System.out.printf("Position : %s\n", annotation.getBoundingPoly());
                // System.out.println(annotation.getBoundingPoly().getVertices());
                BoundingPoly bp = annotation.getBoundingPoly();

                xs.add(bp.getVertices(0).getX());
                xs.add(bp.getVertices(1).getX());
                xs.add(bp.getVertices(2).getX());
                xs.add(bp.getVertices(3).getX());

                ys.add(bp.getVertices(0).getY());
                ys.add(bp.getVertices(1).getY());
                ys.add(bp.getVertices(2).getY());
                ys.add(bp.getVertices(3).getY());

                System.out.println(bp.getVertices(0).getX());
                System.out.println(bp.getVertices(1));
                System.out.println(bp.getVertices(2));
                System.out.println(bp.getVertices(3));

                // Coord format is min x- max x - min y - max y
                Integer minx = Collections.min(xs);
                Integer maxx = Collections.max(xs);

                Integer miny = Collections.min(ys);
                Integer maxy = Collections.max(ys);

                cropCoordStr = minx.toString() + "-" + maxx.toString() + "-" + miny.toString() + "-" + maxy.toString();
                System.out.println(cropCoordStr);

                // System.out.println(bp.getVertices());
                // for (Vertex v: bp.getVertices()) {
                //     System.out.println(v.getX());
                //     System.out.println(v.getY());
                // }
                // System.out.printf(annotation.getBoundingPoly());
                System.out.printf("Text: %s\n", annotation.getDescription());
                // System.out.println();
                String currentText = annotation.getDescription();
                System.out.println();

                // System.out.println(currentText);
                // System.out.println(isNumeric(currentText));

                if (isNumeric(currentText)) {    
                    amounts.add(currentText);
                } else {
                    merchants.add(currentText);                    
                }
            }
            // System.out.println("Do we exit the loop first?");

            // System.out.println(amounts);
            // System.out.println(merchants);

            if (amounts.size() > 0) {
                amount = new BigDecimal(amounts.get(amounts.size() - 1));
            }
            
            if (merchants.size() > 0) {
                merchantName = merchants.get(1); // grab the second element because the first element is everything joined by the newline character
                // System.out.println();
                // System.out.println("This is the first element from merchants arraylist");
                // System.out.println(merchantName);
                // System.out.println();
            }
        
            // System.out.println("assigned everything to variables");
            // System.out.println(merchants.size());
            // // System.out.println(annotation.getDescription();
            // System.out.println("Merchant: " + merchantName);
            // System.out.println(amounts.size());
            // System.out.println("Amount: " + amount.toString());
            // System.out.println("iterating the second time to print everything");
            //   // For full list of available annotations, see http://g.co/cloud/vision/docs
            //   for (EntityAnnotation annotation : res.getLabelAnnotationsList()) {
            //     annotation.getAllFields().forEach((k, v) -> System.out.printf("%s : %s\n", k, v.toString()));
            // }

            //TextAnnotation fullTextAnnotation = res.getFullTextAnnotation();

            return new ReceiptSuggestionResponse(merchantName, amount, cropCoordStr);
        } 
    }
}
