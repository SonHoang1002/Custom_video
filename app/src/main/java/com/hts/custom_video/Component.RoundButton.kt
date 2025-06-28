import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ImportOptionItem(
//    mediaSrc: Int, // Thay bằng Int nếu dùng resource ID, hoặc String nếu dùng đường dẫn khác
    backgroundColor: Color,
    title: String,
    painterId: Int,
    onTap: () -> Unit,
    iconColor: Color? = null,
    textColor: Color? = null,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .border(2.dp, color = Color.Red, shape = RoundedCornerShape(20.dp))
            .clickable(onClick = onTap)
            .padding(10.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(120.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(65.dp)
                    .clip(CircleShape)
                    .background(backgroundColor)
            ) {
                Image(
                    painter = painterResource(painterId),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    colorFilter = iconColor?.let { ColorFilter.tint(it) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Phần text với background bo góc
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(backgroundColor)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                //                .wrapContentWidth(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = textColor ?: Color.Unspecified,
                        textAlign = TextAlign.Center,

                        ),
                    softWrap = true,
                )
            }
        }
    }
}