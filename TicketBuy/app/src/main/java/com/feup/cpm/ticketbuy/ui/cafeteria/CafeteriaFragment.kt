package com.feup.cpm.ticketbuy.ui.cafeteria

import QRCodeDialogFragment
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.app.AlertDialog
import android.graphics.Bitmap
import android.widget.ScrollView
import com.feup.cpm.ticketbuy.R
import com.feup.cpm.ticketbuy.controllers.Controller
import com.feup.cpm.ticketbuy.controllers.cypher.KeyManager
import com.feup.cpm.ticketbuy.controllers.utils.QRCodeGenerator
import com.feup.cpm.ticketbuy.controllers.utils.TagInfo
import com.feup.cpm.ticketbuy.controllers.utils.handleBackPressed
import com.feup.cpm.ticketbuy.models.Item
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject

class CafeteriaFragment : Fragment() {

    private var controller = Controller
    private lateinit var rootLayout: LinearLayout
    private val itemQuantities = mutableMapOf<Int, Int>() // Map to store item quantities
    private var fab: FloatingActionButton? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val scrollView = ScrollView(requireContext())
        rootLayout = LinearLayout(requireContext())
        rootLayout.orientation = LinearLayout.VERTICAL


        scrollView.addView(rootLayout)

        controller.items.value?.forEach { item ->
            val itemLayout = createItemLayout(inflater, container, item)
            rootLayout.addView(itemLayout)
        }

        fab = requireActivity().findViewById(R.id.fab)

        fab?.setOnClickListener(null)
        // change fab icon to shopping cart

        fab?.setImageResource(R.drawable.ic_shopping_cart)
        fab?.setOnClickListener {view ->

            // Iterate through all child views of rootLayout
            for (i in 0 until rootLayout.childCount) {
                val itemLayout = rootLayout.getChildAt(i) as? ScrollView
                val linearLayout = itemLayout?.getChildAt(0) as? LinearLayout

                // Find the EditText view in each itemLayout
                val editText = linearLayout?.findViewById<EditText>(R.id.itemsEditText)

                // Get the quantity entered by the user
                val quantity = editText?.text.toString().toIntOrNull()

                // If quantity is not null and greater than zero, add it to the map
                quantity?.takeIf { it > 0 }?.let {
                    // Here, you can get the ID of the item from your item model
                    val itemId = controller.items.value?.get(i)?.itemId ?: -1
                    if (itemId != -1) {
                        itemQuantities[itemId] = it
                    }
                }
            }

            // Now selectedItems map contains the quantity selected for each item

            Snackbar.make(view, "Selected items: $itemQuantities", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()

            showQRCodeDialog()
        }

        return scrollView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleBackPressed()

        // Observe changes to items LiveData
        controller.items.observe(viewLifecycleOwner) { updatedItems ->
            // Clear existing views
            rootLayout.removeAllViews()

            // Add new item views
            updatedItems.forEach { item ->
                val itemLayout = createItemLayout(
                    LayoutInflater.from(context),
                    null,
                    item
                )
                rootLayout.addView(itemLayout)
            }
        }

        // Trigger fetching items
        controller.getCafeteriaItems()
    }
    @SuppressLint("SetTextI18n")
    private fun createItemLayout(inflater: LayoutInflater, container: ViewGroup?, item: Item): View {
        val scrollView = inflater.inflate(R.layout.fragment_cafeteria, container, false) as ScrollView
        val linearLayout = scrollView.getChildAt(0) as LinearLayout

        val nameTextView = linearLayout.findViewById<TextView>(R.id.nameTextView)
        nameTextView.text = item.name

        val quantityTextView = linearLayout.findViewById<TextView>(R.id.quantityAvailableTextView)
        quantityTextView.text = item.quantity.toString()

        val priceTextView = linearLayout.findViewById<TextView>(R.id.priceTextView)
        priceTextView.text = item.price.toString() + "€"

        return scrollView
    }


    private fun showConfirmationDialog(name: String, numItems: Int, totalCost: Int, item: Item) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirm Purchase")
            .setMessage("Do you want to buy $numItems $name? Total cost: $totalCost€")
            .setPositiveButton("Yes") { dialog, _ ->
                showToast("Buying $numItems $name. Total cost: $totalCost€")
                //Create add purchase
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        // Remove click listener to prevent memory leaks
        fab?.setOnClickListener(null)
        fab?.setImageResource(R.drawable.ic_menu_gallery)
        fab?.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }
    private fun showQRCodeDialog() {
        // Create the payload JSON object
        val payloadJson = JSONObject()
        val stringItemQuantities = itemQuantities.mapKeys { it.key.toString() }

        payloadJson.put("selectedItems", JSONObject(stringItemQuantities))
        payloadJson.put("signature", KeyManager.singData(payloadJson.toString().toByteArray()))

        // Create the TagInfo object
        val tagInfo = TagInfo(
            tagId = "cAFT ORDER 123", // You can generate a unique tagId if needed
            status = true,
            tagType = "Cafeteria",
            userId = controller.userID, // Using the userId from the controller
            payLoad = payloadJson
        )

        // Convert the TagInfo object to JSON string
        val tagInfoJsonString = tagInfo.toJsonString()

        // Generate QR code
        val width = resources.getDimensionPixelSize(R.dimen.qr_code_width)
        val height = resources.getDimensionPixelSize(R.dimen.qr_code_height)
        val qrCodeBitmap: Bitmap? = QRCodeGenerator.generateQRCode(tagInfoJsonString, width, height)

        qrCodeBitmap?.let {
            val dialogFragment = QRCodeDialogFragment(it)
            dialogFragment.show(requireActivity().supportFragmentManager, "QRCodeDialog")
        }
    }

    // Add an extension function to convert TagInfo to JSON string
    private fun TagInfo.toJsonString(): String {
        return JSONObject(
            mapOf(
                "tagId" to tagId,
                "status" to status,
                "tagType" to tagType,
                "userId" to userId,
                "payLoad" to payLoad
            )
        ).toString()
    }

}
