const Input: React.FC<React.InputHTMLAttributes<HTMLInputElement>> = ({ className, ...props }) =>
    <input className={`w-auto border border-gray-300 rounded px-2 py-1 ${className}`} {...props} />
export default Input