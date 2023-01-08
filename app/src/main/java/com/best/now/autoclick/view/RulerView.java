package com.best.now.autoclick.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import com.best.now.autoclick.R;
import com.best.now.autoclick.bean.Coordinate;
import com.best.now.autoclick.ext.CommonExtKt;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Iterator;

/**
 * Screen size independent ruler view.
 */
public class RulerView extends View {

	private Unit unit;
	private Unit unitInch;

	private DisplayMetrics dm;
	private SparseArray<PointF> activePointers;

	private Paint scalePaint;
	private Paint textPaint;
	private Paint labelPaint;
	private Paint backgroundPaint;
	private Paint rulerPaint;
	private Paint showBackPaint;
	private Paint pointerPaint;

	private float guideScaleTextSize;
	private float graduatedScaleWidth;
	private float graduatedScaleBaseLength;
	private int scaleColor;

	private float labelTextSize;
	private String defaultLabelText;
	private int labelColor;

	private int backgroundColor;

	private float pointerRadius;
	private float pointerStrokeWidth;
	private int pointerColor;

	/**
	 * Creates a new RulerView.
	 */
	public RulerView(Context context) {
		this(context, null);
	}

	public RulerView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RulerView(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	public RulerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);

		final TypedArray a = context.obtainStyledAttributes(
				attrs, R.styleable.RulerView, defStyleAttr, defStyleRes);

		guideScaleTextSize = a.getDimension(R.styleable.RulerView_guideScaleTextSize, 40);
		graduatedScaleWidth = a.getDimension(R.styleable.RulerView_graduatedScaleWidth, CommonExtKt.dp2px(this,2));
		graduatedScaleBaseLength =
				a.getDimension(R.styleable.RulerView_graduatedScaleBaseLength, CommonExtKt.dp2px(this,20));
		scaleColor = a.getColor(R.styleable.RulerView_scaleColor, 0xFF03070A);

		labelTextSize = a.getDimension(R.styleable.RulerView_labelTextSize, 60);
		defaultLabelText = a.getString(R.styleable.RulerView_defaultLabelText);
		if (defaultLabelText == null) {
			defaultLabelText = "Measure with two fingers";
		}
		labelColor = a.getColor(R.styleable.RulerView_labelColor, 0xFF03070A);

		backgroundColor = a.getColor(R.styleable.RulerView_backgroundColor, 0xFFF2EFE7);

		pointerColor = a.getColor(R.styleable.RulerView_pointerColor, 0xFF03070A);
		pointerRadius = a.getDimension(R.styleable.RulerView_pointerRadius, 60);
		pointerStrokeWidth = a.getDimension(R.styleable.RulerView_pointerStrokeWidth, 8);

		dm = getResources().getDisplayMetrics();
		unit = new Unit(dm.ydpi);
		unitInch = new Unit(dm.ydpi);
		unit.setType(a.getInt(R.styleable.RulerView_unit, 1));
		unitInch.setType(a.getInt(R.styleable.RulerView_unit, 0));
		a.recycle();

		initRulerView();
		postDelayed(new Runnable() {
			@Override
			public void run() {
				caculatePoint(new Coordinate(300f,500f),true);
			}
		},500L);
	}
	private void initRulerView() {
		activePointers = new SparseArray<>();

		scalePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		scalePaint.setStrokeWidth(graduatedScaleWidth);
		scalePaint.setTextSize(guideScaleTextSize);
		scalePaint.setColor(scaleColor);

		labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		labelPaint.setTextSize(labelTextSize);
		labelPaint.setColor(labelColor);

		backgroundPaint = new Paint();
		backgroundPaint.setColor(backgroundColor);
		rulerPaint = new Paint();

		showBackPaint = new Paint();
		showBackPaint.setColor(0x80E8F1FE);

		textPaint = new Paint();
		textPaint.setColor(0xFF000000);
		textPaint.setTextSize(60f);
		textPaint.setFakeBoldText(true);

		pointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		pointerPaint.setColor(pointerColor);
		pointerPaint.setStrokeWidth(pointerStrokeWidth);
		pointerPaint.setStyle(Paint.Style.STROKE);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getAction()) {
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				break;
			case MotionEvent.ACTION_DOWN:
				onTouchBegain(new Coordinate(event.getX(), event.getY()));
				break;
			case MotionEvent.ACTION_MOVE:
				onTouchMove(new Coordinate(event.getX(), event.getY()));
				break;
		}
		return true;
	}
	private void onTouchMove(Coordinate coordinate) {
		caculatePoint(coordinate,false);
	}
	private void onTouchBegain(Coordinate coordinate) {
		Log.e("Coordinate:",coordinate.getX()+":"+coordinate.getY());
		caculatePoint(coordinate,true);
	}
	private Coordinate point;
	private Coordinate pointBitmap;
	private Coordinate pointBitmap2;
	private Coordinate point2;
	boolean movePoint1 = true;
	private int picSize = CommonExtKt.dp2px(this,48);
	private float kedu;
	private float kedu2;
	private void caculatePoint(Coordinate coordinate,boolean down) {
		float dx = getWidth();
		float dy = coordinate.getY() ;
		if (point==null){
			point2 = new Coordinate(0, CommonExtKt.dp2px(this,80));
			pointBitmap2 = new Coordinate(dx/2-picSize/2f,CommonExtKt.dp2px(this,80)-picSize/2f);
		}
		if (down){
			if (Math.abs(dy-kedu)>Math.abs(dy-kedu2)){
				movePoint1 = false;
			}else{
				movePoint1 = true;
			}
		}
		if (movePoint1){
			kedu = dy;
			point = new Coordinate(dx, dy);
			pointBitmap = new Coordinate(dx/2-picSize/2f,dy-picSize/2f);
		}else{
			kedu2 = dy;
			point2 = new Coordinate(dx, dy);
			pointBitmap2 = new Coordinate(dx/2-picSize/2f,dy-picSize/2f);
		}
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		int width = getWidth();
		int height = getHeight();
		int paddingTop = getPaddingTop();
		int paddingLeft = getPaddingLeft();

		// Draw background.
//		canvas.drawPaint(backgroundPaint);
		RectF mRectF = new RectF(0, 0, CommonExtKt.dp2px(this,80), height);
		RectF mRectF2 = new RectF(width-CommonExtKt.dp2px(this,80), 0, width, height);
		rulerPaint.setShader(new LinearGradient(0,0,0,mRectF.bottom,0xFFC6B0DD,0xFF9DB8DC, Shader.TileMode.CLAMP));
		canvas.drawRect(mRectF,rulerPaint);
		canvas.drawRect(mRectF2,rulerPaint);
		if (point!=null){
			RectF mRectF3 = new RectF(CommonExtKt.dp2px(this,120), height/2-CommonExtKt.dp2px(this,182)/2, CommonExtKt.dp2px(this,170), height/2+CommonExtKt.dp2px(this,182)/2);
			RectF mRectF4 = new RectF(width-CommonExtKt.dp2px(this,170), height/2-CommonExtKt.dp2px(this,182)/2, width-CommonExtKt.dp2px(this,120), height/2+CommonExtKt.dp2px(this,182)/2);
			canvas.drawRoundRect(mRectF3,CommonExtKt.dp2px(this,8),CommonExtKt.dp2px(this,8),showBackPaint);
			canvas.drawRoundRect(mRectF4,CommonExtKt.dp2px(this,8),CommonExtKt.dp2px(this,8),showBackPaint);
			float cm = Math.abs(point.getY()-point2.getY()) / unit.getPixelsPerUnit();
			float inch = Math.abs(point.getY()-point2.getY()) / unitInch.getPixelsPerUnit();
			String text = unit.getStringRepresentation(cm);
			String textInch = unitInch.getStringRepresentation(inch);
			canvas.save();
			canvas.translate(width-CommonExtKt.dp2px(this,145) - textPaint.getTextSize()/2, height/2 - textPaint.measureText(text) / 2);
			canvas.rotate(90);
			canvas.drawText(text, 0, 0, textPaint);
			canvas.restore();
			canvas.save();
			canvas.translate(CommonExtKt.dp2px(this,145) + textPaint.getTextSize()/2, height/2 + textPaint.measureText(text) / 2);
			canvas.rotate(270);
			canvas.drawText(textInch, 0, 0, textPaint);
			canvas.restore();
		}
		// Draw scale.
		Iterator<Unit.Graduation> pixelsIterator = unit.getPixelIterator(height - paddingTop,Unit.CM);
		Iterator<Unit.Graduation> pixelsInchIterator = unitInch.getPixelIterator(height - paddingTop,Unit.INCH);
		while(pixelsIterator.hasNext()) {
			Unit.Graduation graduation = pixelsIterator.next();

			float startX = width - graduation.relativeLength * graduatedScaleBaseLength;
			float startY = paddingTop + graduation.pixelOffset;
			float endX = width;
			float endY = startY;
			canvas.drawLine(startX, startY, endX, endY, scalePaint);

			if (graduation.value!=0 && graduation.value % 1 == 0) {

				String text = (int) graduation.value + "";

				canvas.save();
				canvas.translate(
						startX - guideScaleTextSize, startY - scalePaint.measureText(text) / 2);
				canvas.rotate(90);
				canvas.drawText(text, 0, 0, scalePaint);
				canvas.restore();
			}
		}
		while(pixelsInchIterator.hasNext()) {
			Unit.Graduation graduation = pixelsInchIterator.next();

			float startX = 0;
			float startY = height-(paddingTop + graduation.pixelOffset);
			float endX = graduation.relativeLength * graduatedScaleBaseLength;
			float endY = startY;
			canvas.drawLine(startX, startY, endX, endY, scalePaint);

			if (graduation.value!=0 && graduation.value % 1 == 0) {
				String text = (int) graduation.value + "";

				canvas.save();
				canvas.translate(
						endX + guideScaleTextSize, startY + scalePaint.measureText(text) / 2);
				canvas.rotate(270);
				canvas.drawText(text, 0, 0, scalePaint);
				canvas.restore();
			}
		}

		// Draw active pointers.
		PointF topPointer = null;
		PointF bottomPointer = null;
		for (int i = 0, numberOfPointers = activePointers.size(); i < numberOfPointers; i++) {
			PointF pointer = activePointers.valueAt(i);
			if (topPointer == null || topPointer.y < pointer.y) {
				topPointer = pointer;
			}
			if (bottomPointer == null || bottomPointer.y > pointer.y) {
				bottomPointer = pointer;
			}
			canvas.drawArc(
					pointer.x - pointerRadius,
					pointer.y - pointerRadius,
					pointer.x + pointerRadius,
					pointer.y + pointerRadius,
					0f,
					360f,
					false,
					pointerPaint);
		}

		/*if (topPointer != null) {
			canvas.drawLine(
					topPointer.x + pointerRadius,
					topPointer.y,
					width,
					topPointer.y,
					pointerPaint);
			canvas.drawLine(
					bottomPointer.x + pointerRadius,
					bottomPointer.y,
					width,
					bottomPointer.y,
					pointerPaint);
		}*/

		// Draw Text label.
//		String labelText = defaultLabelText;
//		if (topPointer != null) {
//			float distanceInPixels = Math.abs(topPointer.y - bottomPointer.y);
//			labelText = unit.getStringRepresentation(distanceInPixels / unit.getPixelsPerUnit());
//		}
		Paint paint2 = new Paint();
		paint2.setAntiAlias(true);
		paint2.setColor(0x6fffffff);
		paint2.setStrokeWidth(2);
		if (point != null) {
			Bitmap bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.iv_la),picSize,picSize,false);
			canvas.drawBitmap(bitmap,pointBitmap.getX(),pointBitmap.getY(),paint2);
			canvas.drawLine(0, point.getY(), point.getX(), point.getY(), paint2);
		}
		if (point2 != null) {
			Bitmap bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.iv_la),picSize,picSize,false);
			canvas.drawBitmap(bitmap,pointBitmap2.getX(),pointBitmap2.getY(),paint2);
			canvas.drawLine(0, point2.getY(), point2.getX(), point2.getY(), paint2);
		}
		Paint paint3 = new Paint();
		paint3.setColor(0x4D6F84B0);
		if (point!=null){
			if (point.getY()<point2.getY())
				canvas.drawRect(0,point.getY()+2,point.getX(),point2.getY()-2,paint3);
			else canvas.drawRect(0,point2.getY()+2,point.getX(),point.getY()-2,paint3);

		}
//		canvas.drawText(labelText, paddingLeft, paddingTop + labelTextSize, labelPaint);
	}

	@Override
	protected int getSuggestedMinimumWidth() {
		return 200;
	}

	@Override
	protected int getSuggestedMinimumHeight() {
		return 200;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int minWidth = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
		int width = Math.max(minWidth, MeasureSpec.getSize(widthMeasureSpec));

		int minHeight = getPaddingBottom() + getPaddingTop() + getSuggestedMinimumHeight();
		int height = Math.max(minHeight, MeasureSpec.getSize(heightMeasureSpec));

		setMeasuredDimension(width, height);
	}

	public class Unit {

		class Graduation {
			float value;
			int pixelOffset;
			float relativeLength;
		}

		public static final int INCH = 0;
		public static final int CM = 1;

		private int type = INCH;
		private float dpi;

		Unit(float dpi) {
			this.dpi = dpi;
		}

		public void setType(int type) {
			if (type == INCH || type == CM) {
				this.type = type;
			}
		}

		public String getStringRepresentation(float value) {
			String suffix = "";
			if (type == INCH) {
				suffix =  "Inch";
			} else if (type == CM) {
				suffix = "CM";
			}
			return String.format("%.2f %s", value, suffix);
		}

		public Iterator<Graduation> getPixelIterator(final int numberOfPixels,int type) {
			return new Iterator<Graduation>() {
				int graduationIndex = 0;
				Graduation graduation = new Graduation();

				private float getValue() {
					return graduationIndex * getPrecision();
				}

				private int getPixels() {
					return (int) (getValue() * getPixelsPerUnit());
				}

				@Override
				public boolean hasNext() {
					return getPixels() <= numberOfPixels;
				}

				@Override
				public Graduation next() {
					// Returns the same Graduation object to avoid allocation.
					graduation.value = getValue();
					graduation.pixelOffset = getPixels();
					graduation.relativeLength = getGraduatedScaleRelativeLength(graduationIndex,type);

					graduationIndex ++;
					return graduation;
				}

				@Override
				public void remove() {

				}
			};
		}

		public float getPixelsPerUnit() {
			if (type == INCH) {
				return dpi;
			} else if (type == CM) {
				return dpi / 2.54f;
			}
			return 0;
		}

		private float getPrecision() {
			if (type == INCH) {
				return 1 / 4f;
			} else if (type == CM) {
				return 1 / 10f;
			}
			return 0;
		}

		private float getGraduatedScaleRelativeLength(int graduationIndex,int type) {
			if (type == INCH) {
				if (graduationIndex % 4 == 0) {
					return 1f;
				} else if (graduationIndex% 2 == 0) {
					return 3 / 4f;
				} else {
					return 1 / 2f;
				}
			} else if (type == CM) {
				if (graduationIndex % 10 == 0) {
					return 1;
				} else if (graduationIndex % 5 == 0) {
					return 3 / 4f;
				} else {
					return 1 / 2f;
				}
			}
			return 0;
		}

	}

}