package com.fy.baselibrary.rv.divider;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.DimenRes;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fy.baselibrary.utils.ResUtils;
import com.fy.baselibrary.utils.ScreenUtils;

/**
 * RecycleView GridLayoutManager 样式分割线 (注意：既然使用了 GridLayoutManager 默认每一行的 item 是大于 1 的)
 * Created by fangs on 2017/12/29.
 */
public class GridItemDecoration extends RecyclerView.ItemDecoration {

    private static final int[] ATTRS = new int[]{android.R.attr.listDivider};
    /**
     * 用于绘制间隔样式
     */
    private Drawable mDivider;

    private Builder builder;

    /**
     * 构造分割线
     * @param context
     */
    public GridItemDecoration(Context context, Builder builder) {
        this.builder = builder;
        init(context);
    }

    private void init(Context context){
        // 获取默认主题的属性
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        mDivider = a.getDrawable(0);
        a.recycle();

        if (builder.mSpace == 0) builder.mSpace = mDivider.getIntrinsicHeight();
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        // 获得每个Item的位置
        int itemPosition = parent.getChildAdapterPosition(view);
        if (itemPosition < builder.rvHeaderNum) return;

        itemPosition -= builder.rvHeaderNum;

        if (builder.isSingleLine){
            outRect.top = builder.mSpace;
            outRect.bottom = builder.mSpace;
        }

        //行 只计算 上
        if(itemPosition / builder.column != 0) {//不是第一行
            outRect.top = builder.mSpace;
        }

        // 不设置列间距
        if (builder.isOnlyLineSpacing) return;
        // 列 只计算 左右
        if (itemPosition % builder.column == 0) {//第一列
            outRect.left = 0;
        } else if (itemPosition % builder.column == builder.column - 1) {//最后一列
            outRect.left = builder.mSpace;
            outRect.right = 0;
        } else {//中间若干列
            outRect.left = builder.mSpace;
            outRect.right = 0;
        }
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        if (!builder.isDraw) return;//如果不绘制 则直接结束绘制

        GridLayoutManager layoutManager = (GridLayoutManager) parent.getLayoutManager();
        //没有子view或者没有没有颜色直接return
        if (null == mDivider || layoutManager.getChildCount() == 0) return;

        //水平方向 --》绘制 垂直分割线
        int mChildCount = parent.getChildCount();
        for (int i = 0; i < mChildCount; i++) {
            //获取每个子布局
            View mChildView = parent.getChildAt(i);
            RecyclerView.LayoutParams mLayoutParams = (RecyclerView.LayoutParams) mChildView
                    .getLayoutParams();

            if (i % builder.column == builder.column -1){//最后一列 则绘制右侧边界分割线
                int left = mChildView.getRight();
                int right = left + builder.mSpace;
                int top = mChildView.getTop();
                int bottom = mChildView.getBottom() + builder.mSpace;

                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }

            int left = mChildView.getLeft() - builder.mSpace;
            int right = left + builder.mSpace;
            int top = mChildView.getTop();
            int bottom = mChildView.getBottom() + builder.mSpace;

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }

        //垂直方向 --》绘制 水平分割线
        for (int i = 0; i < mChildCount; i++) {
            //获取每个子布局
            View mChildView = parent.getChildAt(i);
            RecyclerView.LayoutParams mLayoutParams = (RecyclerView.LayoutParams) mChildView
                    .getLayoutParams();

            if (i / builder.column == 0) {//第一行
                int left = mChildView.getLeft() - builder.mSpace;
                int right = mChildView.getRight() + builder.mSpace;
                int top = mChildView.getTop() - builder.mSpace  ;
                int bottom = top + builder.mSpace;

                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }

            int left = mChildView.getLeft();
            int right = mChildView.getRight();
            int top = mChildView.getBottom() ;
            int bottom = top + builder.mSpace;

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }


    public static class Builder {
        /**
         * 是否 绘制
         */
        private boolean isDraw = true;

        /**
         * 是否 单行
         */
        private boolean isSingleLine = false;

        /** 是否只设置 行间距 */
        private boolean isOnlyLineSpacing = false;

        /**
         * 设置间隔 宽度 单位是dp;
         * （如果参数不为 0，则表示 只设置间隔；
         * 为 0，则表示按系统配置的 listDivider 设置间隔，和绘制分割线）
         */
        private int mSpace;

        /**
         * 网格样式 列数
         */
        private int column = 3;

        /**
         * recyclerView 添加头 数量(避免 因为添加了列表头 而引起 分割线的绘制没有按照预想的 显示)
         * 所以 请求头的 间隔 需要在布局里面设置
         */
        private int rvHeaderNum = 0;

        public Builder setDraw(boolean draw) {
            isDraw = draw;
            return this;
        }

        public Builder setmSpace(@DimenRes int dimenId) {
            this.mSpace = (int) ResUtils.getDimen(dimenId);
            return this;
        }

        public Builder setColumn(int column) {
            this.column = column;
            return this;
        }

        public Builder setRvHeaderNum(int rvHeaderNum) {
            this.rvHeaderNum = rvHeaderNum;
            return this;
        }

        public Builder setSingleLine(boolean isSingleLine) {
            this.isSingleLine = isSingleLine;
            return this;
        }

        public Builder setOnlyLineSpacing(boolean isOnlyLineSpacing) {
            this.isOnlyLineSpacing = isOnlyLineSpacing;
            return this;
        }

        /**
         * 入口
         * @return
         */
        public static Builder init(){
            return new Builder();
        }

        /**
         * 创建 ItemDecoration
         * 【注意：此分割线，只控制 行与行、列与列 直接的 间隔，recycleview 内间距，自行在布局使用 padding设置】
         *  使用 getItemWidth() 计算item 边长【GridLayoutManager】
         *  最后 创建 recycleView 适配器 时候 把计算的 item 边长 传递过去，在 适配器里面 动态设置 item 边长
         */
        public GridItemDecoration create(Context context){
            return new GridItemDecoration(context, this);
        }
    }


    /**
     * GridLayoutManager 计算列表边长
     * @param column                列表 列数
     * @param leftAndRightPadding   列表 左右间隔，没有传 0dp
     * @param itemSpace             列表 item 之间的间隔
     */
    public static int getItemWidth(int column, @DimenRes int leftAndRightPadding, @DimenRes int itemSpace){
        int itemWidth = (int) (ScreenUtils.getScreenWidth() - 2 * ResUtils.getDimen(leftAndRightPadding) - (column - 1) * ResUtils.getDimen(itemSpace));
        itemWidth /= column;//item 边长
        return itemWidth;
    }
}
