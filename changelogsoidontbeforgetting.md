# 1.2.0
- properly clear things before every reload (bugfix)
- you can change pattern stroke orders as well!
    - due to how hex code works, this is not compatible with special handlers like Numerical Reflection and Bookkeeper's Gambit. Things such as those would have to be implemented on a per-pattern basis which is pretty tiring.
# 1.3.0
- `iterate` function
- `Enchantment` type
- `opsConsumed` variable
- per world patterns work with Configu-cast pattern renaming now
- functions didn't use `loop`'s variables inside themselves, for whatever reason
    - also some ABI additions
    - one change: the variables passed in may override `casterExists` etc. (you can override the builtins now basically)
- `$toplevel.$field` syntax made global instead of being just for lists