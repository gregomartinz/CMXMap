package cmx.acuntia.es.cmxmap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.widget.ImageView;


public class MyImageView extends ImageView {
    public MyImageView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        canvas.drawCircle(10,10,10,p);

    }
}
