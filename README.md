


Use the class library, can achieve the effect of the side menu like QQ





Use the class library, can achieve the effect of the side menu like QQ


this is example:

xml:

<com.administrator.similarqqslidingmenu.DragLayout
       android:id="@+id/dl" >

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:paddingBottom="50dp"
        android:paddingLeft="10dp"
        android:paddingRight="50dp"
        android:paddingTop="50dp" >


    </LinearLayout>

    <RelativeLayout
            android:layout_width="match_parent"
            android:layout_height="50dp"
            android:background="#18B6EF" >


    </RelativeLayout>



</com.administrator.similarqqslidingmenu.DragLayout>



at java :





        mDl = (DragLayout) findViewById(R.id.dl);


        mDl.setOnDragStateChangeListener(new DragLayout.OnDragStateChangeListener() {

            @Override
            public void onOpen() {

            }

            @Override
            public void onDragging(float percent) {

            }

            @Override
            public void onClose() {

            }
        });
    }


![image](SimilarQQSlidingMenus/QQ图片20151105000638.png)
![image](SimilarQQSlidingMenus/QQ图片20151105000645.png)
![image](SimilarQQSlidingMenus/QQ图片20151105000653.png)