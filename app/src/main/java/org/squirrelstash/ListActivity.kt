package org.squirrelstash

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_list.*
import kotlinx.android.synthetic.main.content_list.*

class ListActivity : SecuredActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        setSupportActionBar(toolbar)

        add.setOnClickListener { view ->
            signOut()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSignOut() {
        signIn()
    }

    override fun onUser(user: FirebaseUser?) {
        super.onUser(user)

        contentText?.text = user?.displayName ?: "No user"

        if (user != null) {
            Snackbar.make(add, "User: ${user.displayName}", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        } else {
            Snackbar.make(add, "No user !", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }
}
