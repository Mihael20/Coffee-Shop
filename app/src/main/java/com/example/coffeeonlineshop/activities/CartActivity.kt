package com.example.coffeeonlineshop.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coffeeonlineshop.Helper.ChangeNumberItemsListener
import com.example.coffeeonlineshop.Helper.ManagmentCart
import com.example.coffeeonlineshop.Repository.OrderRepository
import com.example.coffeeonlineshop.adapters.CartAdapter
import com.example.coffeeonlineshop.databinding.ActivityCartBinding

class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private lateinit var managmentCart: ManagmentCart
    private var tax: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        managmentCart = ManagmentCart(this)

        calculateCart()
        setVariable()
        initCartList()
    }

    private fun initCartList() {
        binding.apply {
            listView.layoutManager = LinearLayoutManager(
                this@CartActivity,
                LinearLayoutManager.VERTICAL,
                false
            )
            listView.adapter = CartAdapter(
                listItemSelected = managmentCart.getListCart(),
                context = this@CartActivity,
                changeNumberItemsListener = object : ChangeNumberItemsListener {
                    override fun onChanged() {
                        calculateCart()
                    }
                }
            )
        }
    }

    private fun setVariable() {
        binding.backBtn.setOnClickListener { finish() }

        binding.processBtn.setOnClickListener {
            val orderRepo = OrderRepository()
            orderRepo.saveOrder(
                items = managmentCart.getListCart(),
                totalPrice = managmentCart.getTotalFee()
            )
            android.app.AlertDialog.Builder(this)
                .setTitle("Order Placed! ☕")
                .setMessage("Your order has been placed successfully!")
                .setPositiveButton("OK") { _, _ ->
                    finish()
                }
                .show()
        }
    }

    private fun calculateCart() {
        val percentTax = 0.02
        val delivery = 10.0
        tax = ((managmentCart.getTotalFee() * percentTax) * 100) / 100
        val total = ((managmentCart.getTotalFee() + tax + delivery) * 100) / 100
        val itemTotal = (managmentCart.getTotalFee() * 100) / 100

        binding.apply {
            totalFeeTxt.text = "$$itemTotal"
            totalTaxTxt.text = "$$tax"
            deliveryTxt.text = "$$delivery"
            totalTxt.text = "$$total"
        }
    }
}