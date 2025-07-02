package com.posprinter.printdemo.activity

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.posprinter.printdemo.App
import com.posprinter.printdemo.R
import com.posprinter.printdemo.databinding.ActivityZplBinding
import net.posprinter.ZPLConst
import net.posprinter.ZPLPrinter

/**
 * @author: star
 * @date: 2022-05-27
 */
class ZplActivity : AppCompatActivity() {
    private val printer = ZPLPrinter(App.get().curConnect)
    private lateinit var bind: ActivityZplBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityZplBinding.inflate(layoutInflater)
        setContentView(bind.root)
        initClick()
    }

    private fun initClick() {
        bind.textBtn.setOnClickListener {
            printer.addStart()
                .setPrinterWidth(500)
                .setLabelLength(400)
                .addText(10, 0, ZPLConst.FNT_A, ZPLConst.ROTATION_0, 9,5, "fontA")
                .addText(10, 20, ZPLConst.FNT_B, ZPLConst.ROTATION_0, 11,7, "fontB")
                .addText(10, 40, ZPLConst.FNT_C, ZPLConst.ROTATION_0, 18,10, "fontC")
                .addText(10, 60, ZPLConst.FNT_D, ZPLConst.ROTATION_0, 18,10, "fontD")
                .addText(10, 90, ZPLConst.FNT_F, ZPLConst.ROTATION_0, 26,13, "fontF")
                .addText(10, 130,  "fontE")
                .addText(10, 160, ZPLConst.FNT_G, ZPLConst.ROTATION_0, 60,40, "fontG")
                .addText(10, 220, ZPLConst.FNT_0, ZPLConst.ROTATION_0, 15,12, "font0")
                .addText(350, 250, ZPLConst.FNT_E, ZPLConst.ROTATION_0, 28,15, "E0")
                .addText(250, 250, ZPLConst.FNT_E, ZPLConst.ROTATION_90, 28,15, "E90")
                .addText(250,350, ZPLConst.FNT_E, ZPLConst.ROTATION_180, 28,15, "E180")
                .addText(350, 350, ZPLConst.FNT_E, ZPLConst.ROTATION_270, 28,15, "E270")
                .addEnd()
        }
        bind.customFontBtn.setOnClickListener {
            printer.setCharSet("UTF-8")
            printer.addStart()
                .setCustomFont("LZHONGHEI.TTF", '1', ZPLConst.CODE_PAGE_UTF8)
                .addText(0, 0, '1', 24,24, "custom Font")
                .addText(100, 100, '1', ZPLConst.ROTATION_90, 24,24, "customFont 90")
                .addEnd()
        }
        bind.sampleBtn.setOnClickListener {
            printer.addStart()
                .addBox(10,10, 378, 239, 5)
                .addBox(20, 20, 360, 219, 5)
                .addQRCode(30,30, "https://www.google.com/")
                .addText(40,130, "Reverse")
                .addReverse(32, 122, 150, 40, 2)
                .downloadBitmap(110, "SAMPLE.GRF", BitmapFactory.decodeResource(resources, R.drawable.nv_test))
                .addBitmap(180, 30, "SAMPLE.GRF")
                .addBox(30, 180, 330, 2, 2)
                .addText(40,190, "Hello World")
                .addPrintCount(2)
                .addEnd()
        }
        bind.barcodeBtn.setOnClickListener {
            printer.addStart()
                .setPrinterWidth(500)
                .addText(0, 0,"Code11")
                .addBarcode(0,30, ZPLConst.BCS_CODE11, ZPLConst.ROTATION_0, ZPLConst.HRI_TEXT_BELOW, "123456", 2, 50)
                .addText(0, 120, ZPLConst.FNT_D, "interleaved 2 of 5")
                .addBarcode(0,150, ZPLConst.BCS_INTERLEAVED2OF5, ZPLConst.ROTATION_0, ZPLConst.HRI_TEXT_BELOW, "123456", 2, 50)
                .addText(0, 240, "Code39")
                .addBarcode(0,270, ZPLConst.BCS_CODE39, ZPLConst.ROTATION_0, ZPLConst.HRI_TEXT_BELOW, "123ABC", 2, 50)
                .addText(0, 360, "EAN8")
                .addBarcode(0,390, ZPLConst.BCS_EAN8, ZPLConst.ROTATION_0, ZPLConst.HRI_TEXT_BELOW, "1234567", 2, 50)

                .addText(260, 0, "code128")
                .addBarcode(260,30, ZPLConst.BCS_CODE128, ZPLConst.ROTATION_0, ZPLConst.HRI_TEXT_BELOW, "123456", 2, 50)
                .addText(240, 120, "CODE93")
                .addBarcode(200,150, ZPLConst.BCS_CODE93, ZPLConst.ROTATION_0, ZPLConst.HRI_TEXT_BELOW, "12345ABCDE", 2, 50)
                .addText(300, 240, "UPCE")
                .addBarcode(300,270, ZPLConst.BCS_UPCE, ZPLConst.ROTATION_0, ZPLConst.HRI_TEXT_BELOW, "1230000045", 2, 50)
                .addText(250, 360, "EAN13")
                .addBarcode(250,390, ZPLConst.BCS_EAN13, ZPLConst.ROTATION_0, ZPLConst.HRI_TEXT_BELOW, "12345678", 2, 50)
                .addEnd()
        }

        bind.barcode2Btn.setOnClickListener {
            printer.addStart()
                .addText(0, 0, "CODABAR")
                .addBarcode(0,30, ZPLConst.BCS_CODABAR, ZPLConst.ROTATION_0, ZPLConst.HRI_TEXT_BELOW, "A123456D", 2, 50)
                .addText(0, 120, "MSI")
                .addBarcode(0,150, ZPLConst.BCS_MSI, ZPLConst.ROTATION_0, ZPLConst.HRI_TEXT_BELOW, "123456", 2, 50)
                .addText(0, 240, "PLESSEY")
                .addBarcode(0,270, ZPLConst.BCS_PLESSEY, ZPLConst.ROTATION_0, ZPLConst.HRI_TEXT_BELOW, "12345", 2, 50)
                .addText(0, 360, "UPCA")
                .addBarcode(0,390, ZPLConst.BCS_UPCA, ZPLConst.ROTATION_0, ZPLConst.HRI_TEXT_BELOW, "07000002198", 2, 50)
                .addEnd()
        }
    }


}