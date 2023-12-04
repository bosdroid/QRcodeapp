import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class JSONResponse(
    @SerializedName("generatedUrl")
    val generatedUrl: String
):Serializable