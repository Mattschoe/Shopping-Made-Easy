package weberstudio.app.billigsteprodukter.logic

data class Store(val name: String, val products: Set<Product> = HashSet<Product>())