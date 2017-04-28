package xyz.dowenliu.ketcd

/**
 * Etcd Endpoint info
 *
 * create at 2017/4/13
 * @author liufl
 * @since 0.1.0
 *
 * @property host The host part of the endpoint url.
 * @property port The port part of the endpoint url.
 * @property schema The schema type of the endpoint url. Can only be http or https for now.
 */
data class Endpoint internal constructor(val host: String, val port: Int, val schema: Schema) {
    companion object {
        /**
         * Construct an Endpoint instance with specific schema, host and port
         *
         * @param host Endpoint host address. Should not be blank.
         * @param port Endpoint port. Should be between 0 and 65535
         * @param schema Endpoint schema. Http as default.
         * @return An Endpoint instance with specified host and port
         * @throw IllegalArgumentException If host or port given is invalid
         */
        @JvmStatic fun ofDetail(host: String, port: Int, schema: Schema = Schema.Http): Endpoint {
            require(host.isNotBlank(), { "Endpoint's host should not be empty" })
            require(port >= 0 || port <= 65535, { "Endpoint's port should be between 0 and 65535." })
            return Endpoint(host.trim(), port, schema)
        }

        /**
         * Construct an Endpoint instance with specific endpoint address expression.
         *
         * Expression formats:
         * > _host_:_port_
         * > _schema_://_host_:_port_
         *
         * _schema_ should only be http or https, default http
         * _host_ should not be blank
         * _port_ should be an integer value between 0 and 65535
         *
         * @param endpoint Endpoint address expression.
         * @return An Endpoint instance created by the specified expression.
         * @throws IllegalArgumentException If the expression given breaks the rule above.
         * @throws NullPointerException If the expression given do not contain an port part or it's not a valid integer.
         */
        @JvmStatic fun of(endpoint: String): Endpoint {
            val schemaAndAddress = endpoint.split("://")
            val hostAndPort: List<String>
            val schema: Schema
            if (schemaAndAddress.size == 1) {
                // no schema part
                schema = schemaOf(null)
                hostAndPort = schemaAndAddress[0].split(':')
            } else if (schemaAndAddress.size == 2) {
                schema = schemaOf(schemaAndAddress[0])
                hostAndPort = schemaAndAddress[1].split(':')
            } else
                throw IllegalArgumentException("Expecting endpoint expression formatting" +
                        " 'host:port' or 'schema://host:port' but got '$endpoint' ")
            require(hostAndPort.size == 2, { "Expecting endpoint expression formatting" +
                    " 'host:port' or 'schema://host:port' but got '$endpoint' " })
            require(hostAndPort[0].isNotBlank(), { "Endpoint's host should not be empty" })
            val port = requireNotNull(hostAndPort[1].toIntOrNull(),
                    { "Endpoint expression's 'port' is not a number for input '$endpoint'" })
            require(port >= 0 || port <= 65535, { "Endpoint's port should be between 0 and 65535." })
            return Endpoint(hostAndPort[0].trim(), port, schema)
        }

        private fun schemaOf(schema: String?): Schema {
            if (schema == null) return Schema.Http
            if ("http" == schema.toLowerCase()) return Schema.Http
            if ("https" == schema.toLowerCase()) return Schema.Https
            throw IllegalArgumentException("Unknown schema name: $schema")
        }
    }

    /**
     * @return A string representation of the endpoint. Can be used to create an [Endpoint] by [of].
     */
    override fun toString(): String = "${schema.value}://$host:$port"

    /**
     * Endpoint schema. Can only be http or https for now.
     *
     * @property value The string value of the schema.
     */
    enum class Schema(val value: String) {
        /**
         * The HTTP schema.
         */
        Http("http"),
        /**
         * The HTTPS schema.
         */
        Https("https"),
    }
}